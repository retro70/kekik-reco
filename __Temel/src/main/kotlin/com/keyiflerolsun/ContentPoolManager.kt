package com.keyiflerolsun

import android.util.Log
import com.lagradost.cloudstream3.*
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Benzersiz içerik havuzunu yöneten ana sınıf
 */
class ContentPoolManager {
    
    companion object {
        private const val TAG = "ContentPoolManager"
        private const val SIMILARITY_THRESHOLD = 0.7
        private const val CACHE_EXPIRY_TIME = 24 * 60 * 60 * 1000L // 24 saat
    }
    
    // İçerik havuzu - ID -> ContentItem
    private val contentPool = ConcurrentHashMap<String, ContentItem>()
    
    // Kaynak listesi - kaynak adı -> MainAPI instance
    private val sources = mutableMapOf<String, MainAPI>()
    
    // Cache yönetimi
    private val cacheTimestamps = ConcurrentHashMap<String, Long>()
    
    /**
     * Kaynak ekler
     */
    fun addSource(sourceName: String, source: MainAPI) {
        sources[sourceName] = source
        Log.d(TAG, "Kaynak eklendi: $sourceName")
    }
    
    /**
     * Tüm kaynaklardan arama yapar ve benzersiz içerik havuzu oluşturur
     */
    suspend fun searchAndMerge(query: String): List<ContentItem> {
        Log.d(TAG, "Arama başlatılıyor: $query")
        
        val searchResults = mutableListOf<SearchResult>()
        
        // Tüm kaynaklardan paralel arama
        val searchJobs = sources.map { (sourceName, source) ->
            async {
                try {
                    val results = source.search(query)
                    results.map { result ->
                        SearchResult(
                            title = result.name,
                            url = result.url,
                            type = result.type,
                            poster = result.posterUrl,
                            year = result.year,
                            sourceName = sourceName
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Kaynak arama hatası ($sourceName): ${e.message}")
                    emptyList<SearchResult>()
                }
            }
        }
        
        // Tüm arama sonuçlarını topla
        searchJobs.awaitAll().forEach { results ->
            searchResults.addAll(results)
        }
        
        Log.d(TAG, "Toplam ${searchResults.size} sonuç bulundu")
        
        // Sonuçları birleştir ve benzersiz içerik havuzu oluştur
        return mergeSearchResults(searchResults)
    }
    
    /**
     * Arama sonuçlarını birleştirir ve benzersiz içerik havuzu oluşturur
     */
    private fun mergeSearchResults(results: List<SearchResult>): List<ContentItem> {
        val mergedItems = mutableListOf<ContentItem>()
        val processedIds = mutableSetOf<String>()
        
        results.forEach { result ->
            val normalizedTitle = ContentNormalizer.normalizeTitle(result.title)
            val contentId = ContentNormalizer.generateId(result.title)
            
            // Eğer bu ID daha önce işlenmişse, mevcut öğeye kaynak ekle
            if (processedIds.contains(contentId)) {
                val existingItem = mergedItems.find { it.id == contentId }
                existingItem?.let { item ->
                    val newSource = ContentSource(
                        sourceName = result.sourceName,
                        sourceUrl = result.url,
                        originalTitle = result.title,
                        quality = ContentNormalizer.extractQuality(result.title),
                        language = "tr"
                    )
                    
                    // Kaynak zaten var mı kontrol et
                    if (!item.sources.any { it.sourceName == result.sourceName }) {
                        val updatedItem = item.copy(
                            sources = item.sources + newSource,
                            lastUpdated = System.currentTimeMillis()
                        )
                        val index = mergedItems.indexOf(item)
                        mergedItems[index] = updatedItem
                    }
                }
            } else {
                // Yeni benzersiz içerik oluştur
                val contentSource = ContentSource(
                    sourceName = result.sourceName,
                    sourceUrl = result.url,
                    originalTitle = result.title,
                    quality = ContentNormalizer.extractQuality(result.title),
                    language = "tr"
                )
                
                val contentItem = ContentItem(
                    id = contentId,
                    title = ContentNormalizer.cleanTitleFromQuality(
                        ContentNormalizer.cleanTitleFromYear(result.title)
                    ),
                    normalizedTitle = normalizedTitle,
                    type = result.type,
                    year = result.year ?: ContentNormalizer.extractYear(result.title),
                    poster = result.poster,
                    sources = listOf(contentSource)
                )
                
                mergedItems.add(contentItem)
                processedIds.add(contentId)
            }
        }
        
        Log.d(TAG, "Birleştirme tamamlandı: ${mergedItems.size} benzersiz içerik")
        return mergedItems.sortedByDescending { it.sources.size } // En çok kaynağı olan önce
    }
    
    /**
     * Benzerlik bazında eşleştirme yapar (daha gelişmiş)
     */
    private fun mergeBySimilarity(results: List<SearchResult>): List<ContentItem> {
        val mergedItems = mutableListOf<ContentItem>()
        val processedResults = mutableSetOf<SearchResult>()
        
        results.forEach { result ->
            if (processedResults.contains(result)) return@forEach
            
            val similarResults = mutableListOf<SearchResult>()
            similarResults.add(result)
            processedResults.add(result)
            
            // Benzer sonuçları bul
            results.forEach { otherResult ->
                if (!processedResults.contains(otherResult) && 
                    ContentNormalizer.isMatch(result.title, otherResult.title, SIMILARITY_THRESHOLD)) {
                    similarResults.add(otherResult)
                    processedResults.add(otherResult)
                }
            }
            
            // Benzer sonuçları birleştir
            val firstResult = similarResults.first()
            val contentId = ContentNormalizer.generateId(firstResult.title)
            
            val sources = similarResults.map { searchResult ->
                ContentSource(
                    sourceName = searchResult.sourceName,
                    sourceUrl = searchResult.url,
                    originalTitle = searchResult.title,
                    quality = ContentNormalizer.extractQuality(searchResult.title),
                    language = "tr"
                )
            }
            
            val contentItem = ContentItem(
                id = contentId,
                title = ContentNormalizer.cleanTitleFromQuality(
                    ContentNormalizer.cleanTitleFromYear(firstResult.title)
                ),
                normalizedTitle = ContentNormalizer.normalizeTitle(firstResult.title),
                type = firstResult.type,
                year = firstResult.year ?: ContentNormalizer.extractYear(firstResult.title),
                poster = firstResult.poster,
                sources = sources
            )
            
            mergedItems.add(contentItem)
        }
        
        return mergedItems.sortedByDescending { it.sources.size }
    }
    
    /**
     * Cache'den içerik alır
     */
    fun getCachedContent(query: String): List<ContentItem>? {
        val cacheKey = "search_${ContentNormalizer.generateId(query)}"
        val timestamp = cacheTimestamps[cacheKey] ?: return null
        
        if (System.currentTimeMillis() - timestamp > CACHE_EXPIRY_TIME) {
            cacheTimestamps.remove(cacheKey)
            return null
        }
        
        return contentPool.values.filter { item ->
            ContentNormalizer.isMatch(query, item.title, 0.5)
        }.toList()
    }
    
    /**
     * Cache'e içerik kaydeder
     */
    private fun cacheContent(query: String, items: List<ContentItem>) {
        val cacheKey = "search_${ContentNormalizer.generateId(query)}"
        cacheTimestamps[cacheKey] = System.currentTimeMillis()
        
        items.forEach { item ->
            contentPool[item.id] = item
        }
    }
    
    /**
     * Cache'i temizler
     */
    fun clearCache() {
        contentPool.clear()
        cacheTimestamps.clear()
        Log.d(TAG, "Cache temizlendi")
    }
    
    /**
     * ID'ye göre içerik alır
     */
    fun getContentById(id: String): ContentItem? {
        return contentPool[id]
    }
    
    /**
     * Kaynak eklentisini alır
     */
    fun getSourcePlugin(sourceName: String): MainAPI? {
        return sources[sourceName]
    }
    
    /**
     * İstatistikleri döndürür
     */
    fun getStats(): Map<String, Any> {
        return mapOf(
            "totalContent" to contentPool.size,
            "totalSources" to sources.size,
            "cacheSize" to cacheTimestamps.size,
            "sources" to sources.keys.toList(),
            "typeStats" to getTypeStats(),
            "genreStats" to getGenreStats()
        )
    }
    
    /**
     * Tür bazlı istatistikleri döndürür
     */
    fun getTypeStats(): Map<String, Int> {
        return contentPool.values
            .groupBy { it.type.name }
            .mapValues { it.value.size }
    }
    
    /**
     * Kategori/tür bazlı istatistikleri döndürür
     */
    fun getGenreStats(): Map<String, Int> {
        return contentPool.values
            .flatMap { it.tags }
            .groupBy { it }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(20) // En popüler 20 kategori
            .toMap()
    }
    
    /**
     * Yıl bazlı istatistikleri döndürür
     */
    fun getYearStats(): Map<Int, Int> {
        return contentPool.values
            .mapNotNull { it.year }
            .groupBy { it }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.first }
            .toMap()
    }
    
    /**
     * Tüm içerikleri döndürür
     */
    fun getAllContent(): List<ContentItem> {
        return contentPool.values.toList()
    }
} 