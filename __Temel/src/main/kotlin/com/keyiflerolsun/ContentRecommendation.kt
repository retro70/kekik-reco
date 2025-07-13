package com.keyiflerolsun

import android.util.Log
import com.lagradost.cloudstream3.TvType
import kotlin.math.abs

/**
 * Akıllı içerik önerileri ve tür önerileri sistemi
 */
object ContentRecommendation {
    
    private const val TAG = "ContentRecommendation"
    
    /**
     * İçerik bazlı öneriler
     */
    fun getContentRecommendations(contentItem: ContentItem, allContent: List<ContentItem>, limit: Int = 10): List<ContentItem> {
        Log.d(TAG, "İçerik önerileri hesaplanıyor: ${contentItem.title}")
        
        val recommendations = mutableListOf<Pair<ContentItem, Double>>()
        
        allContent.forEach { otherContent ->
            if (otherContent.id != contentItem.id) {
                val similarity = calculateContentSimilarity(contentItem, otherContent)
                if (similarity > 0.3) { // Minimum benzerlik eşiği
                    recommendations.add(Pair(otherContent, similarity))
                }
            }
        }
        
        return recommendations
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
    }
    
    /**
     * İki içerik arasındaki benzerliği hesaplar
     */
    private fun calculateContentSimilarity(content1: ContentItem, content2: ContentItem): Double {
        var similarity = 0.0
        var totalWeight = 0.0
        
        // Tür benzerliği (ağırlık: 0.3)
        if (content1.type == content2.type) {
            similarity += 0.3
        }
        totalWeight += 0.3
        
        // Yıl benzerliği (ağırlık: 0.2)
        if (content1.year != null && content2.year != null) {
            val yearDiff = abs(content1.year - content2.year)
            val yearSimilarity = when {
                yearDiff == 0 -> 1.0
                yearDiff <= 2 -> 0.8
                yearDiff <= 5 -> 0.6
                yearDiff <= 10 -> 0.4
                else -> 0.2
            }
            similarity += yearSimilarity * 0.2
        }
        totalWeight += 0.2
        
        // Etiket benzerliği (ağırlık: 0.3)
        val commonTags = content1.tags.intersect(content2.tags.toSet())
        val tagSimilarity = if (content1.tags.isNotEmpty() && content2.tags.isNotEmpty()) {
            commonTags.size.toDouble() / maxOf(content1.tags.size, content2.tags.size)
        } else 0.0
        similarity += tagSimilarity * 0.3
        totalWeight += 0.3
        
        // Başlık benzerliği (ağırlık: 0.2)
        val titleSimilarity = ContentNormalizer.calculateSimilarity(content1.title, content2.title)
        similarity += titleSimilarity * 0.2
        totalWeight += 0.2
        
        return if (totalWeight > 0) similarity / totalWeight else 0.0
    }
    
    /**
     * Kullanıcı arama geçmişine göre tür önerileri
     */
    fun getGenreRecommendations(searchHistory: List<String>, allContent: List<ContentItem>, limit: Int = 10): List<String> {
        Log.d(TAG, "Tür önerileri hesaplanıyor")
        
        val genreFrequency = mutableMapOf<String, Int>()
        
        // Arama geçmişindeki içeriklerin türlerini analiz et
        searchHistory.forEach { query ->
            allContent.forEach { content ->
                if (ContentNormalizer.isMatch(query, content.title, 0.5)) {
                    content.tags.forEach { tag ->
                        genreFrequency[tag] = genreFrequency.getOrDefault(tag, 0) + 1
                    }
                }
            }
        }
        
        return genreFrequency
            .toList()
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
    }
    
    /**
     * Popüler türleri döndürür
     */
    fun getPopularGenres(allContent: List<ContentItem>, limit: Int = 15): List<String> {
        val genreFrequency = mutableMapOf<String, Int>()
        
        allContent.forEach { content ->
            content.tags.forEach { tag ->
                genreFrequency[tag] = genreFrequency.getOrDefault(tag, 0) + 1
            }
        }
        
        return genreFrequency
            .toList()
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
    }
    
    /**
     * Yeni çıkan içerikleri döndürür
     */
    fun getRecentContent(allContent: List<ContentItem>, limit: Int = 20): List<ContentItem> {
        val currentYear = java.time.LocalDate.now().year
        
        return allContent
            .filter { it.year != null && it.year >= currentYear - 2 }
            .sortedByDescending { it.year }
            .take(limit)
    }
    
    /**
     * Yüksek puanlı içerikleri döndürür
     */
    fun getTopRatedContent(allContent: List<ContentItem>, limit: Int = 20): List<ContentItem> {
        return allContent
            .filter { it.rating != null && it.rating >= 7 }
            .sortedByDescending { it.rating }
            .take(limit)
    }
    
    /**
     * Çoklu kaynaklı içerikleri döndürür (en çok kaynağı olan)
     */
    fun getMultiSourceContent(allContent: List<ContentItem>, limit: Int = 20): List<ContentItem> {
        return allContent
            .filter { it.sources.size >= 3 }
            .sortedByDescending { it.sources.size }
            .take(limit)
    }
    
    /**
     * Kategori bazlı öneriler
     */
    fun getCategoryRecommendations(category: String, allContent: List<ContentItem>, limit: Int = 15): List<ContentItem> {
        return allContent
            .filter { content ->
                content.tags.any { it.contains(category, ignoreCase = true) }
            }
            .sortedByDescending { it.rating ?: 0 }
            .take(limit)
    }
    
    /**
     * Yıl bazlı öneriler
     */
    fun getYearRecommendations(year: Int, allContent: List<ContentItem>, limit: Int = 15): List<ContentItem> {
        return allContent
            .filter { it.year == year }
            .sortedByDescending { it.rating ?: 0 }
            .take(limit)
    }
    
    /**
     * Karma öneriler (çeşitli kriterlere göre)
     */
    fun getMixedRecommendations(allContent: List<ContentItem>, limit: Int = 20): List<ContentItem> {
        val recommendations = mutableListOf<ContentItem>()
        
        // Yeni çıkanlar (%30)
        val recentCount = (limit * 0.3).toInt()
        recommendations.addAll(getRecentContent(allContent, recentCount))
        
        // Yüksek puanlılar (%40)
        val topRatedCount = (limit * 0.4).toInt()
        val topRated = getTopRatedContent(allContent, topRatedCount)
        recommendations.addAll(topRated.filter { it !in recommendations })
        
        // Çoklu kaynaklılar (%30)
        val remainingCount = limit - recommendations.size
        if (remainingCount > 0) {
            val multiSource = getMultiSourceContent(allContent, remainingCount)
            recommendations.addAll(multiSource.filter { it !in recommendations })
        }
        
        return recommendations.take(limit)
    }
} 