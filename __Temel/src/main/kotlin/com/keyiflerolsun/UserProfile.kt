package com.keyiflerolsun

import android.util.Log
import com.lagradost.cloudstream3.TvType
import java.util.*

/**
 * Kullanıcı profili ve kişiselleştirme sistemi
 */
object UserProfile {
    
    private const val TAG = "UserProfile"
    
    /**
     * Kullanıcı profili
     */
    data class UserProfile(
        val userId: String,
        val username: String,
        val preferences: UserPreferences,
        val watchHistory: List<WatchHistoryItem>,
        val favoriteGenres: List<String>,
        val favoriteTypes: List<TvType>,
        val createdAt: Long = System.currentTimeMillis(),
        val lastActive: Long = System.currentTimeMillis()
    )
    
    /**
     * Kullanıcı tercihleri
     */
    data class UserPreferences(
        val language: String = "tr",
        val quality: String = "HD",
        val autoPlay: Boolean = false,
        val showSubtitles: Boolean = true,
        val theme: String = "dark",
        val notifications: Boolean = true,
        val maxResultsPerPage: Int = 20,
        val defaultSort: String = "RELEVANCE"
    )
    
    /**
     * İzleme geçmişi
     */
    data class WatchHistoryItem(
        val contentId: String,
        val title: String,
        val type: TvType,
        val watchDate: Long = System.currentTimeMillis(),
        val watchDuration: Int = 0, // dakika
        val completed: Boolean = false,
        val rating: Int? = null,
        val sourceName: String? = null
    )
    
    /**
     * Kullanıcı istatistikleri
     */
    data class UserStats(
        val totalWatched: Int,
        val totalHours: Int,
        val favoriteGenre: String?,
        val favoriteType: TvType?,
        val mostWatchedSource: String?,
        val averageRating: Double,
        val completionRate: Double
    )
    
    // Kullanıcı profilleri (gerçek uygulamada veritabanında saklanır)
    private val userProfiles = mutableMapOf<String, UserProfile>()
    
    /**
     * Kullanıcı profili oluşturur
     */
    fun createUserProfile(userId: String, username: String): UserProfile {
        val profile = UserProfile(
            userId = userId,
            username = username,
            preferences = UserPreferences(),
            watchHistory = emptyList(),
            favoriteGenres = emptyList(),
            favoriteTypes = emptyList()
        )
        
        userProfiles[userId] = profile
        Log.d(TAG, "Kullanıcı profili oluşturuldu: $username")
        
        return profile
    }
    
    /**
     * Kullanıcı profili alır
     */
    fun getUserProfile(userId: String): UserProfile? {
        return userProfiles[userId]
    }
    
    /**
     * Kullanıcı tercihlerini günceller
     */
    fun updateUserPreferences(userId: String, preferences: UserPreferences): Boolean {
        val profile = userProfiles[userId] ?: return false
        
        val updatedProfile = profile.copy(
            preferences = preferences,
            lastActive = System.currentTimeMillis()
        )
        
        userProfiles[userId] = updatedProfile
        Log.d(TAG, "Kullanıcı tercihleri güncellendi: $userId")
        
        return true
    }
    
    /**
     * İzleme geçmişine ekler
     */
    fun addToWatchHistory(userId: String, contentItem: ContentItem, sourceName: String? = null): Boolean {
        val profile = userProfiles[userId] ?: return false
        
        val historyItem = WatchHistoryItem(
            contentId = contentItem.id,
            title = contentItem.title,
            type = contentItem.type,
            sourceName = sourceName
        )
        
        val updatedHistory = (profile.watchHistory + historyItem)
            .sortedByDescending { it.watchDate }
            .take(1000) // Son 1000 izleme
        
        val updatedProfile = profile.copy(
            watchHistory = updatedHistory,
            lastActive = System.currentTimeMillis()
        )
        
        userProfiles[userId] = updatedProfile
        Log.d(TAG, "İzleme geçmişine eklendi: ${contentItem.title}")
        
        return true
    }
    
    /**
     * İzleme geçmişini günceller
     */
    fun updateWatchHistory(userId: String, contentId: String, watchDuration: Int, completed: Boolean, rating: Int? = null): Boolean {
        val profile = userProfiles[userId] ?: return false
        
        val updatedHistory = profile.watchHistory.map { item ->
            if (item.contentId == contentId) {
                item.copy(
                    watchDuration = watchDuration,
                    completed = completed,
                    rating = rating ?: item.rating
                )
            } else {
                item
            }
        }
        
        val updatedProfile = profile.copy(
            watchHistory = updatedHistory,
            lastActive = System.currentTimeMillis()
        )
        
        userProfiles[userId] = updatedProfile
        Log.d(TAG, "İzleme geçmişi güncellendi: $contentId")
        
        return true
    }
    
    /**
     * Favori kategorileri günceller
     */
    fun updateFavoriteGenres(userId: String, genres: List<String>): Boolean {
        val profile = userProfiles[userId] ?: return false
        
        val updatedProfile = profile.copy(
            favoriteGenres = genres,
            lastActive = System.currentTimeMillis()
        )
        
        userProfiles[userId] = updatedProfile
        Log.d(TAG, "Favori kategoriler güncellendi: $userId")
        
        return true
    }
    
    /**
     * Favori türleri günceller
     */
    fun updateFavoriteTypes(userId: String, types: List<TvType>): Boolean {
        val profile = userProfiles[userId] ?: return false
        
        val updatedProfile = profile.copy(
            favoriteTypes = types,
            lastActive = System.currentTimeMillis()
        )
        
        userProfiles[userId] = updatedProfile
        Log.d(TAG, "Favori türler güncellendi: $userId")
        
        return true
    }
    
    /**
     * Kullanıcı istatistiklerini hesaplar
     */
    fun getUserStats(userId: String): UserStats? {
        val profile = userProfiles[userId] ?: return null
        
        val totalWatched = profile.watchHistory.size
        val totalHours = profile.watchHistory.sumOf { it.watchDuration } / 60
        val completedCount = profile.watchHistory.count { it.completed }
        val completionRate = if (totalWatched > 0) completedCount.toDouble() / totalWatched else 0.0
        
        // En favori kategori
        val genreFrequency = mutableMapOf<String, Int>()
        profile.watchHistory.forEach { item ->
            // Bu kısım gerçek uygulamada ContentItem'dan kategori bilgisi alır
        }
        val favoriteGenre = genreFrequency.maxByOrNull { it.value }?.key
        
        // En favori tür
        val typeFrequency = profile.watchHistory.groupBy { it.type }.mapValues { it.value.size }
        val favoriteType = typeFrequency.maxByOrNull { it.value }?.key
        
        // En çok izlenen kaynak
        val sourceFrequency = profile.watchHistory
            .mapNotNull { it.sourceName }
            .groupBy { it }
            .mapValues { it.value.size }
        val mostWatchedSource = sourceFrequency.maxByOrNull { it.value }?.key
        
        // Ortalama puan
        val ratings = profile.watchHistory.mapNotNull { it.rating }
        val averageRating = if (ratings.isNotEmpty()) ratings.average() else 0.0
        
        return UserStats(
            totalWatched = totalWatched,
            totalHours = totalHours,
            favoriteGenre = favoriteGenre,
            favoriteType = favoriteType,
            mostWatchedSource = mostWatchedSource,
            averageRating = averageRating,
            completionRate = completionRate
        )
    }
    
    /**
     * Kişiselleştirilmiş öneriler
     */
    fun getPersonalizedRecommendations(userId: String, allContent: List<ContentItem>, limit: Int = 20): List<ContentItem> {
        val profile = userProfiles[userId] ?: return emptyList()
        val stats = getUserStats(userId) ?: return emptyList()
        
        val recommendations = mutableListOf<ContentItem>()
        
        // 1. Favori kategorilerden öneriler (%40)
        if (profile.favoriteGenres.isNotEmpty()) {
            val genreCount = (limit * 0.4).toInt()
            val genreRecommendations = allContent
                .filter { content ->
                    content.tags.any { tag ->
                        profile.favoriteGenres.any { favorite ->
                            tag.contains(favorite, ignoreCase = true)
                        }
                    }
                }
                .filter { it.id !in profile.watchHistory.map { h -> h.contentId } }
                .sortedByDescending { it.rating ?: 0 }
                .take(genreCount)
            
            recommendations.addAll(genreRecommendations)
        }
        
        // 2. Favori türlerden öneriler (%30)
        if (profile.favoriteTypes.isNotEmpty()) {
            val typeCount = (limit * 0.3).toInt()
            val typeRecommendations = allContent
                .filter { content ->
                    profile.favoriteTypes.contains(content.type)
                }
                .filter { it.id !in profile.watchHistory.map { h -> h.contentId } }
                .sortedByDescending { it.rating ?: 0 }
                .take(typeCount)
            
            recommendations.addAll(typeRecommendations.filter { it !in recommendations })
        }
        
        // 3. Benzer kullanıcıların izledikleri (%20)
        val similarCount = (limit * 0.2).toInt()
        val similarRecommendations = getSimilarUserRecommendations(userId, allContent, similarCount)
        recommendations.addAll(similarRecommendations.filter { it !in recommendations })
        
        // 4. Trend içerikler (%10)
        val remainingCount = limit - recommendations.size
        if (remainingCount > 0) {
            val trendRecommendations = ContentRecommendation.getMixedRecommendations(allContent, remainingCount)
            recommendations.addAll(trendRecommendations.filter { it !in recommendations })
        }
        
        return recommendations.take(limit)
    }
    
    /**
     * Benzer kullanıcıların önerileri
     */
    private fun getSimilarUserRecommendations(userId: String, allContent: List<ContentItem>, limit: Int): List<ContentItem> {
        val currentProfile = userProfiles[userId] ?: return emptyList()
        
        // Basit benzerlik algoritması - gerçek uygulamada daha gelişmiş olabilir
        val similarUsers = userProfiles.values
            .filter { it.userId != userId }
            .filter { it.favoriteGenres.any { genre -> currentProfile.favoriteGenres.contains(genre) } }
            .sortedByDescending { user ->
                val commonGenres = user.favoriteGenres.intersect(currentProfile.favoriteGenres.toSet()).size
                commonGenres
            }
            .take(5)
        
        val recommendedContentIds = similarUsers
            .flatMap { it.watchHistory }
            .filter { it.completed && (it.rating ?: 0) >= 7 }
            .map { it.contentId }
            .distinct()
        
        return allContent
            .filter { it.id in recommendedContentIds }
            .filter { it.id !in currentProfile.watchHistory.map { h -> h.contentId } }
            .take(limit)
    }
    
    /**
     * Kullanıcı aktivite geçmişi
     */
    fun getUserActivityHistory(userId: String, days: Int = 30): List<WatchHistoryItem> {
        val profile = userProfiles[userId] ?: return emptyList()
        val cutoffDate = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        
        return profile.watchHistory
            .filter { it.watchDate >= cutoffDate }
            .sortedByDescending { it.watchDate }
    }
    
    /**
     * Kullanıcı profili siler
     */
    fun deleteUserProfile(userId: String): Boolean {
        return userProfiles.remove(userId) != null
    }
    
    /**
     * Tüm kullanıcı profillerini alır (admin için)
     */
    fun getAllUserProfiles(): List<UserProfile> {
        return userProfiles.values.toList()
    }
} 