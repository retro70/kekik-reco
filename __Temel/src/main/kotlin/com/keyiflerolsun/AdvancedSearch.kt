package com.keyiflerolsun

import android.util.Log
import com.lagradost.cloudstream3.TvType

/**
 * Gelişmiş arama sistemi - filtreler, sıralama, sayfalama
 */
object AdvancedSearch {
    
    private const val TAG = "AdvancedSearch"
    
    /**
     * Arama filtresi
     */
    data class SearchFilter(
        val query: String = "",
        val type: TvType? = null,
        val genres: List<String> = emptyList(),
        val yearFrom: Int? = null,
        val yearTo: Int? = null,
        val ratingFrom: Int? = null,
        val ratingTo: Int? = null,
        val minSources: Int = 1,
        val maxSources: Int? = null,
        val language: String? = null,
        val quality: String? = null
    )
    
    /**
     * Sıralama seçenekleri
     */
    enum class SortOption {
        RELEVANCE,      // Alaka düzeyi
        TITLE_ASC,      // Başlık (A-Z)
        TITLE_DESC,     // Başlık (Z-A)
        YEAR_ASC,       // Yıl (eski-yeni)
        YEAR_DESC,      // Yıl (yeni-eski)
        RATING_ASC,     // Puan (düşük-yüksek)
        RATING_DESC,    // Puan (yüksek-düşük)
        SOURCES_ASC,    // Kaynak sayısı (az-çok)
        SOURCES_DESC,   // Kaynak sayısı (çok-az)
        RECENT,         // En yeni
        POPULAR         // En popüler
    }
    
    /**
     * Gelişmiş arama sonucu
     */
    data class AdvancedSearchResult(
        val content: List<ContentItem>,
        val totalCount: Int,
        val page: Int,
        val pageSize: Int,
        val totalPages: Int,
        val appliedFilters: SearchFilter,
        val sortOption: SortOption
    )
    
    /**
     * Gelişmiş arama yapar
     */
    fun advancedSearch(
        allContent: List<ContentItem>,
        filter: SearchFilter,
        sortOption: SortOption = SortOption.RELEVANCE,
        page: Int = 1,
        pageSize: Int = 20
    ): AdvancedSearchResult {
        Log.d(TAG, "Gelişmiş arama başlatılıyor: $filter")
        
        // 1. Filtreleme
        var filteredContent = allContent.filter { content ->
            matchesFilter(content, filter)
        }
        
        // 2. Sıralama
        filteredContent = sortContent(filteredContent, sortOption)
        
        // 3. Sayfalama
        val totalCount = filteredContent.size
        val totalPages = (totalCount + pageSize - 1) / pageSize
        val startIndex = (page - 1) * pageSize
        val endIndex = minOf(startIndex + pageSize, totalCount)
        
        val pagedContent = if (startIndex < totalCount) {
            filteredContent.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
        
        Log.d(TAG, "Gelişmiş arama tamamlandı: ${pagedContent.size} sonuç (sayfa $page/$totalPages)")
        
        return AdvancedSearchResult(
            content = pagedContent,
            totalCount = totalCount,
            page = page,
            pageSize = pageSize,
            totalPages = totalPages,
            appliedFilters = filter,
            sortOption = sortOption
        )
    }
    
    /**
     * İçeriğin filtreye uyup uymadığını kontrol eder
     */
    private fun matchesFilter(content: ContentItem, filter: SearchFilter): Boolean {
        // Sorgu kontrolü
        if (filter.query.isNotBlank()) {
            val normalizedQuery = ContentNormalizer.normalizeTitle(filter.query)
            val normalizedTitle = content.normalizedTitle
            if (!ContentNormalizer.isMatch(normalizedTitle, normalizedQuery, 0.3)) {
                return false
            }
        }
        
        // Tür kontrolü
        if (filter.type != null && content.type != filter.type) {
            return false
        }
        
        // Kategori kontrolü
        if (filter.genres.isNotEmpty()) {
            val hasMatchingGenre = filter.genres.any { genre ->
                content.tags.any { tag -> tag.contains(genre, ignoreCase = true) }
            }
            if (!hasMatchingGenre) {
                return false
            }
        }
        
        // Yıl aralığı kontrolü
        if (filter.yearFrom != null && (content.year == null || content.year < filter.yearFrom)) {
            return false
        }
        if (filter.yearTo != null && (content.year == null || content.year > filter.yearTo)) {
            return false
        }
        
        // Puan aralığı kontrolü
        if (filter.ratingFrom != null && (content.rating == null || content.rating < filter.ratingFrom)) {
            return false
        }
        if (filter.ratingTo != null && (content.rating == null || content.rating > filter.ratingTo)) {
            return false
        }
        
        // Kaynak sayısı kontrolü
        if (content.sources.size < filter.minSources) {
            return false
        }
        if (filter.maxSources != null && content.sources.size > filter.maxSources) {
            return false
        }
        
        // Dil kontrolü
        if (filter.language != null) {
            val hasMatchingLanguage = content.sources.any { source ->
                source.language?.contains(filter.language, ignoreCase = true) == true
            }
            if (!hasMatchingLanguage) {
                return false
            }
        }
        
        // Kalite kontrolü
        if (filter.quality != null) {
            val hasMatchingQuality = content.sources.any { source ->
                source.quality?.contains(filter.quality, ignoreCase = true) == true
            }
            if (!hasMatchingQuality) {
                return false
            }
        }
        
        return true
    }
    
    /**
     * İçerikleri sıralar
     */
    private fun sortContent(content: List<ContentItem>, sortOption: SortOption): List<ContentItem> {
        return when (sortOption) {
            SortOption.RELEVANCE -> content // Zaten alaka düzeyine göre sıralı
            SortOption.TITLE_ASC -> content.sortedBy { it.title }
            SortOption.TITLE_DESC -> content.sortedByDescending { it.title }
            SortOption.YEAR_ASC -> content.sortedBy { it.year ?: 0 }
            SortOption.YEAR_DESC -> content.sortedByDescending { it.year ?: 0 }
            SortOption.RATING_ASC -> content.sortedBy { it.rating ?: 0 }
            SortOption.RATING_DESC -> content.sortedByDescending { it.rating ?: 0 }
            SortOption.SOURCES_ASC -> content.sortedBy { it.sources.size }
            SortOption.SOURCES_DESC -> content.sortedByDescending { it.sources.size }
            SortOption.RECENT -> content.sortedByDescending { it.lastUpdated }
            SortOption.POPULAR -> content.sortedByDescending { it.rating ?: 0 }
        }
    }
    
    /**
     * Otomatik tamamlama önerileri
     */
    fun getAutoCompleteSuggestions(query: String, allContent: List<ContentItem>, limit: Int = 10): List<String> {
        if (query.length < 2) return emptyList()
        
        val suggestions = mutableSetOf<String>()
        val normalizedQuery = ContentNormalizer.normalizeTitle(query)
        
        allContent.forEach { content ->
            val normalizedTitle = content.normalizedTitle
            if (normalizedTitle.contains(normalizedQuery, ignoreCase = true)) {
                suggestions.add(content.title)
            }
            
            // Kategori önerileri
            content.tags.forEach { tag ->
                if (tag.contains(query, ignoreCase = true)) {
                    suggestions.add(tag)
                }
            }
        }
        
        return suggestions.toList().take(limit)
    }
    
    /**
     * Arama geçmişi yönetimi
     */
    private val searchHistory = mutableListOf<String>()
    private const val MAX_HISTORY_SIZE = 50
    
    fun addToSearchHistory(query: String) {
        if (query.isNotBlank()) {
            searchHistory.remove(query) // Varsa kaldır
            searchHistory.add(0, query) // Başa ekle
            if (searchHistory.size > MAX_HISTORY_SIZE) {
                searchHistory.removeAt(searchHistory.size - 1)
            }
        }
    }
    
    fun getSearchHistory(): List<String> = searchHistory.toList()
    
    fun clearSearchHistory() {
        searchHistory.clear()
    }
    
    /**
     * Popüler aramalar
     */
    fun getPopularSearches(allContent: List<ContentItem>, limit: Int = 10): List<String> {
        val searchFrequency = mutableMapOf<String, Int>()
        
        allContent.forEach { content ->
            // Başlık kelimelerini say
            val words = content.title.split(" ").filter { it.length > 2 }
            words.forEach { word ->
                searchFrequency[word] = searchFrequency.getOrDefault(word, 0) + 1
            }
            
            // Kategorileri say
            content.tags.forEach { tag ->
                searchFrequency[tag] = searchFrequency.getOrDefault(tag, 0) + 1
            }
        }
        
        return searchFrequency
            .toList()
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
    }
    
    /**
     * Benzer aramalar
     */
    fun getSimilarSearches(query: String, allContent: List<ContentItem>, limit: Int = 5): List<String> {
        val similarSearches = mutableListOf<String>()
        val normalizedQuery = ContentNormalizer.normalizeTitle(query)
        
        allContent.forEach { content ->
            val normalizedTitle = content.normalizedTitle
            val similarity = ContentNormalizer.calculateSimilarity(normalizedQuery, normalizedTitle)
            
            if (similarity > 0.5 && content.title != query) {
                similarSearches.add(content.title)
            }
        }
        
        return similarSearches.distinct().take(limit)
    }
} 