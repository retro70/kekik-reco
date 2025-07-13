package com.keyiflerolsun

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.runBlocking

/**
 * Frontend için JSON API endpoint'leri
 */
object JsonApi {
    
    private val mapper = jacksonObjectMapper()
    private val unifiedPlugin = UnifiedSearchPlugin()
    
    /**
     * Arama sonuçlarını JSON formatında döndürür
     */
    fun searchJson(query: String): String {
        return runBlocking {
            try {
                val results = unifiedPlugin.search(query)
                val contentItems = results.map { searchResponse ->
                    // SearchResponse'u ContentItem'a çevir
                    val contentId = searchResponse.url.removePrefix("unified://")
                    unifiedPlugin.getContentById(contentId) ?: createContentItemFromSearchResponse(searchResponse)
                }
                
                val response = mapOf(
                    "success" to true,
                    "query" to query,
                    "totalResults" to contentItems.size,
                    "results" to contentItems.map { it.toJsonMap() }
                )
                
                mapper.writeValueAsString(response)
            } catch (e: Exception) {
                val errorResponse = mapOf(
                    "success" to false,
                    "error" to e.message,
                    "query" to query
                )
                mapper.writeValueAsString(errorResponse)
            }
        }
    }
    
    /**
     * İçerik detaylarını JSON formatında döndürür
     */
    fun getContentDetailsJson(contentId: String): String {
        return runBlocking {
            try {
                val contentItem = unifiedPlugin.getContentById(contentId)
                if (contentItem != null) {
                    val response = mapOf(
                        "success" to true,
                        "content" to contentItem.toJsonMap()
                    )
                    mapper.writeValueAsString(response)
                } else {
                    val errorResponse = mapOf(
                        "success" to false,
                        "error" to "İçerik bulunamadı",
                        "contentId" to contentId
                    )
                    mapper.writeValueAsString(errorResponse)
                }
            } catch (e: Exception) {
                val errorResponse = mapOf(
                    "success" to false,
                    "error" to e.message,
                    "contentId" to contentId
                )
                mapper.writeValueAsString(errorResponse)
            }
        }
    }
    
    /**
     * İstatistikleri JSON formatında döndürür
     */
    fun getStatsJson(): String {
        return try {
            val stats = unifiedPlugin.getStats()
            val response = mapOf(
                "success" to true,
                "stats" to stats
            )
            mapper.writeValueAsString(response)
        } catch (e: Exception) {
            val errorResponse = mapOf(
                "success" to false,
                "error" to e.message
            )
            mapper.writeValueAsString(errorResponse)
        }
    }
    
    /**
     * Tür istatistiklerini JSON formatında döndürür
     */
    fun getTypeStatsJson(): String {
        return try {
            val typeStats = unifiedPlugin.getTypeStats()
            val response = mapOf(
                "success" to true,
                "typeStats" to typeStats
            )
            mapper.writeValueAsString(response)
        } catch (e: Exception) {
            val errorResponse = mapOf(
                "success" to false,
                "error" to e.message
            )
            mapper.writeValueAsString(errorResponse)
        }
    }
    
    /**
     * Kategori istatistiklerini JSON formatında döndürür
     */
    fun getGenreStatsJson(): String {
        return try {
            val genreStats = unifiedPlugin.getGenreStats()
            val response = mapOf(
                "success" to true,
                "genreStats" to genreStats
            )
            mapper.writeValueAsString(response)
        } catch (e: Exception) {
            val errorResponse = mapOf(
                "success" to false,
                "error" to e.message
            )
            mapper.writeValueAsString(errorResponse)
        }
    }
    
    /**
     * Yıl istatistiklerini JSON formatında döndürür
     */
    fun getYearStatsJson(): String {
        return try {
            val yearStats = unifiedPlugin.getYearStats()
            val response = mapOf(
                "success" to true,
                "yearStats" to yearStats
            )
            mapper.writeValueAsString(response)
        } catch (e: Exception) {
            val errorResponse = mapOf(
                "success" to false,
                "error" to e.message
            )
            mapper.writeValueAsString(errorResponse)
        }
    }
    
    /**
     * İçerik önerilerini JSON formatında döndürür
     */
    fun getContentRecommendationsJson(contentId: String, limit: Int = 10): String {
        return runBlocking {
            try {
                val contentItem = unifiedPlugin.getContentById(contentId)
                if (contentItem == null) {
                    val errorResponse = mapOf(
                        "success" to false,
                        "error" to "İçerik bulunamadı",
                        "contentId" to contentId
                    )
                    return@runBlocking mapper.writeValueAsString(errorResponse)
                }
                
                val allContent = unifiedPlugin.getAllContent()
                val recommendations = ContentRecommendation.getContentRecommendations(contentItem, allContent, limit)
                
                val response = mapOf(
                    "success" to true,
                    "contentId" to contentId,
                    "contentTitle" to contentItem.title,
                    "recommendations" to recommendations.map { it.toJsonMap() }
                )
                
                mapper.writeValueAsString(response)
            } catch (e: Exception) {
                val errorResponse = mapOf(
                    "success" to false,
                    "error" to e.message,
                    "contentId" to contentId
                )
                mapper.writeValueAsString(errorResponse)
            }
        }
    }
    
    /**
     * Popüler türleri JSON formatında döndürür
     */
    fun getPopularGenresJson(limit: Int = 15): String {
        return try {
            val allContent = unifiedPlugin.getAllContent()
            val popularGenres = ContentRecommendation.getPopularGenres(allContent, limit)
            
            val response = mapOf(
                "success" to true,
                "popularGenres" to popularGenres
            )
            
            mapper.writeValueAsString(response)
        } catch (e: Exception) {
            val errorResponse = mapOf(
                "success" to false,
                "error" to e.message
            )
            mapper.writeValueAsString(errorResponse)
        }
    }
    
    /**
     * Karma önerileri JSON formatında döndürür
     */
    fun getMixedRecommendationsJson(limit: Int = 20): String {
        return try {
            val allContent = unifiedPlugin.getAllContent()
            val recommendations = ContentRecommendation.getMixedRecommendations(allContent, limit)
            
            val response = mapOf(
                "success" to true,
                "recommendations" to recommendations.map { it.toJsonMap() }
            )
            
            mapper.writeValueAsString(response)
        } catch (e: Exception) {
            val errorResponse = mapOf(
                "success" to false,
                "error" to e.message
            )
            mapper.writeValueAsString(errorResponse)
        }
    }
    
    /**
     * Yeni çıkan içerikleri JSON formatında döndürür
     */
    fun getRecentContentJson(limit: Int = 20): String {
        return try {
            val allContent = unifiedPlugin.getAllContent()
            val recentContent = ContentRecommendation.getRecentContent(allContent, limit)
            
            val response = mapOf(
                "success" to true,
                "recentContent" to recentContent.map { it.toJsonMap() }
            )
            
            mapper.writeValueAsString(response)
        } catch (e: Exception) {
            val errorResponse = mapOf(
                "success" to false,
                "error" to e.message
            )
            mapper.writeValueAsString(errorResponse)
        }
    }
    
    /**
     * Yüksek puanlı içerikleri JSON formatında döndürür
     */
    fun getTopRatedContentJson(limit: Int = 20): String {
        return try {
            val allContent = unifiedPlugin.getAllContent()
            val topRatedContent = ContentRecommendation.getTopRatedContent(allContent, limit)
            
            val response = mapOf(
                "success" to true,
                "topRatedContent" to topRatedContent.map { it.toJsonMap() }
            )
            
            mapper.writeValueAsString(response)
        } catch (e: Exception) {
            val errorResponse = mapOf(
                "success" to false,
                "error" to e.message
            )
            mapper.writeValueAsString(errorResponse)
        }
    }
    
    /**
     * Ana sayfa kategorilerini JSON formatında döndürür
     */
    fun getMainPageCategoriesJson(): String {
        return try {
            val categories = MainPageCategories.getMainPageCategories()
            
            val response = mapOf(
                "success" to true,
                "categories" to categories.map { category ->
                    mapOf(
                        "id" to category.id,
                        "title" to category.title,
                        "description" to category.description,
                        "type" to category.type.name
                    )
                }
            )
            
            mapper.writeValueAsString(response)
        } catch (e: Exception) {
            val errorResponse = mapOf(
                "success" to false,
                "error" to e.message
            )
            mapper.writeValueAsString(errorResponse)
        }
    }
    
    /**
     * Kategori içeriklerini JSON formatında döndürür
     */
    fun getCategoryContentJson(categoryId: String, limit: Int = 20): String {
        return try {
            val allContent = unifiedPlugin.getAllContent()
            val categories = MainPageCategories.getMainPageCategories()
            val category = categories.find { it.id == categoryId }
            
            if (category == null) {
                val errorResponse = mapOf(
                    "success" to false,
                    "error" to "Kategori bulunamadı",
                    "categoryId" to categoryId
                )
                return mapper.writeValueAsString(errorResponse)
            }
            
            val categoryContent = MainPageCategories.getCategoryContent(category, allContent, limit)
            
            val response = mapOf(
                "success" to true,
                "category" to mapOf(
                    "id" to category.id,
                    "title" to category.title,
                    "description" to category.description
                ),
                "content" to categoryContent.map { it.toJsonMap() }
            )
            
            mapper.writeValueAsString(response)
        } catch (e: Exception) {
            val errorResponse = mapOf(
                "success" to false,
                "error" to e.message,
                "categoryId" to categoryId
            )
            mapper.writeValueAsString(errorResponse)
        }
    }
    
    /**
     * Özel koleksiyonları JSON formatında döndürür
     */
    fun getSpecialCollectionsJson(): String {
        return try {
            val collections = MainPageCategories.getSpecialCollections()
            
            val response = mapOf(
                "success" to true,
                "collections" to collections.map { collection ->
                    mapOf(
                        "id" to collection.id,
                        "title" to collection.title,
                        "description" to collection.description
                    )
                }
            )
            
            mapper.writeValueAsString(response)
        } catch (e: Exception) {
            val errorResponse = mapOf(
                "success" to false,
                "error" to e.message
            )
            mapper.writeValueAsString(errorResponse)
        }
    }
    
    /**
     * Özel koleksiyon içeriklerini JSON formatında döndürür
     */
    fun getSpecialCollectionContentJson(collectionId: String, limit: Int = 20): String {
        return try {
            val allContent = unifiedPlugin.getAllContent()
            val collections = MainPageCategories.getSpecialCollections()
            val collection = collections.find { it.id == collectionId }
            
            if (collection == null) {
                val errorResponse = mapOf(
                    "success" to false,
                    "error" to "Koleksiyon bulunamadı",
                    "collectionId" to collectionId
                )
                return mapper.writeValueAsString(errorResponse)
            }
            
            val collectionContent = MainPageCategories.getSpecialCollectionContent(collection, allContent, limit)
            
            val response = mapOf(
                "success" to true,
                "collection" to mapOf(
                    "id" to collection.id,
                    "title" to collection.title,
                    "description" to collection.description
                ),
                "content" to collectionContent.map { it.toJsonMap() }
            )
            
            mapper.writeValueAsString(response)
        } catch (e: Exception) {
            val errorResponse = mapOf(
                "success" to false,
                "error" to e.message,
                "collectionId" to collectionId
            )
            mapper.writeValueAsString(errorResponse)
        }
    }
    
    /**
     * Gelişmiş arama yapar
     */
    fun advancedSearchJson(
        query: String = "",
        type: String? = null,
        genres: List<String> = emptyList(),
        yearFrom: Int? = null,
        yearTo: Int? = null,
        ratingFrom: Int? = null,
        ratingTo: Int? = null,
        minSources: Int = 1,
        maxSources: Int? = null,
        language: String? = null,
        quality: String? = null,
        sortBy: String = "RELEVANCE",
        page: Int = 1,
        pageSize: Int = 20
    ): String {
        return try {
            val allContent = unifiedPlugin.getAllContent()
            
            // Tür dönüşümü
            val tvType = when (type?.lowercase()) {
                "movie" -> com.lagradost.cloudstream3.TvType.Movie
                "tvseries", "series" -> com.lagradost.cloudstream3.TvType.TvSeries
                "anime" -> com.lagradost.cloudstream3.TvType.Anime
                "documentary" -> com.lagradost.cloudstream3.TvType.Documentary
                else -> null
            }
            
            // Sıralama dönüşümü
            val sortOption = when (sortBy.uppercase()) {
                "TITLE_ASC" -> AdvancedSearch.SortOption.TITLE_ASC
                "TITLE_DESC" -> AdvancedSearch.SortOption.TITLE_DESC
                "YEAR_ASC" -> AdvancedSearch.SortOption.YEAR_ASC
                "YEAR_DESC" -> AdvancedSearch.SortOption.YEAR_DESC
                "RATING_ASC" -> AdvancedSearch.SortOption.RATING_ASC
                "RATING_DESC" -> AdvancedSearch.SortOption.RATING_DESC
                "SOURCES_ASC" -> AdvancedSearch.SortOption.SOURCES_ASC
                "SOURCES_DESC" -> AdvancedSearch.SortOption.SOURCES_DESC
                "RECENT" -> AdvancedSearch.SortOption.RECENT
                "POPULAR" -> AdvancedSearch.SortOption.POPULAR
                else -> AdvancedSearch.SortOption.RELEVANCE
            }
            
            val filter = AdvancedSearch.SearchFilter(
                query = query,
                type = tvType,
                genres = genres,
                yearFrom = yearFrom,
                yearTo = yearTo,
                ratingFrom = ratingFrom,
                ratingTo = ratingTo,
                minSources = minSources,
                maxSources = maxSources,
                language = language,
                quality = quality
            )
            
            val result = AdvancedSearch.advancedSearch(
                allContent = allContent,
                filter = filter,
                sortOption = sortOption,
                page = page,
                pageSize = pageSize
            )
            
            val response = mapOf(
                "success" to true,
                "result" to mapOf(
                    "content" to result.content.map { it.toJsonMap() },
                    "totalCount" to result.totalCount,
                    "page" to result.page,
                    "pageSize" to result.pageSize,
                    "totalPages" to result.totalPages,
                    "appliedFilters" to mapOf(
                        "query" to result.appliedFilters.query,
                        "type" to result.appliedFilters.type?.name,
                        "genres" to result.appliedFilters.genres,
                        "yearFrom" to result.appliedFilters.yearFrom,
                        "yearTo" to result.appliedFilters.yearTo,
                        "ratingFrom" to result.appliedFilters.ratingFrom,
                        "ratingTo" to result.appliedFilters.ratingTo,
                        "minSources" to result.appliedFilters.minSources,
                        "maxSources" to result.appliedFilters.maxSources,
                        "language" to result.appliedFilters.language,
                        "quality" to result.appliedFilters.quality
                    ),
                    "sortOption" to result.sortOption.name
                )
            )
            
            mapper.writeValueAsString(response)
        } catch (e: Exception) {
            val errorResponse = mapOf(
                "success" to false,
                "error" to e.message
            )
            mapper.writeValueAsString(errorResponse)
        }
    }
    
    /**
     * Otomatik tamamlama önerilerini JSON formatında döndürür
     */
    fun getAutoCompleteSuggestionsJson(query: String, limit: Int = 10): String {
        return try {
            val allContent = unifiedPlugin.getAllContent()
            val suggestions = AdvancedSearch.getAutoCompleteSuggestions(query, allContent, limit)
            
            val response = mapOf(
                "success" to true,
                "query" to query,
                "suggestions" to suggestions
            )
            
            mapper.writeValueAsString(response)
        } catch (e: Exception) {
            val errorResponse = mapOf(
                "success" to false,
                "error" to e.message,
                "query" to query
            )
            mapper.writeValueAsString(errorResponse)
        }
    }
    
    /**
     * Arama geçmişini JSON formatında döndürür
     */
    fun getSearchHistoryJson(): String {
        return try {
            val history = AdvancedSearch.getSearchHistory()
            
            val response = mapOf(
                "success" to true,
                "history" to history
            )
            
            mapper.writeValueAsString(response)
        } catch (e: Exception) {
            val errorResponse = mapOf(
                "success" to false,
                "error" to e.message
            )
            mapper.writeValueAsString(errorResponse)
        }
    }
    
    /**
     * Popüler aramaları JSON formatında döndürür
     */
    fun getPopularSearchesJson(limit: Int = 10): String {
        return try {
            val allContent = unifiedPlugin.getAllContent()
            val popularSearches = AdvancedSearch.getPopularSearches(allContent, limit)
            
            val response = mapOf(
                "success" to true,
                "popularSearches" to popularSearches
            )
            
            mapper.writeValueAsString(response)
        } catch (e: Exception) {
            val errorResponse = mapOf(
                "success" to false,
                "error" to e.message
            )
            mapper.writeValueAsString(errorResponse)
        }
    }
    
    /**
     * Benzer aramaları JSON formatında döndürür
     */
    fun getSimilarSearchesJson(query: String, limit: Int = 5): String {
        return try {
            val allContent = unifiedPlugin.getAllContent()
            val similarSearches = AdvancedSearch.getSimilarSearches(query, allContent, limit)
            
            val response = mapOf(
                "success" to true,
                "query" to query,
                "similarSearches" to similarSearches
            )
            
            mapper.writeValueAsString(response)
        } catch (e: Exception) {
            val errorResponse = mapOf(
                "success" to false,
                "error" to e.message,
                "query" to query
            )
            mapper.writeValueAsString(errorResponse)
        }
    }
    
    /**
     * Kullanıcı profili oluşturur
     */
    fun createUserProfileJson(userId: String, username: String): String {
        return try {
            val profile = UserProfile.createUserProfile(userId, username)
            
            val response = mapOf(
                "success" to true,
                "profile" to mapOf(
                    "userId" to profile.userId,
                    "username" to profile.username,
                    "createdAt" to profile.createdAt
                )
            )
            
            mapper.writeValueAsString(response)
        } catch (e: Exception) {
            val errorResponse = mapOf(
                "success" to false,
                "error" to e.message
            )
            mapper.writeValueAsString(errorResponse)
        }
    }
    
    /**
     * Kullanıcı profili alır
     */
    fun getUserProfileJson(userId: String): String {
        return try {
            val profile = UserProfile.getUserProfile(userId)
            
            if (profile == null) {
                val errorResponse = mapOf(
                    "success" to false,
                    "error" to "Kullanıcı profili bulunamadı",
                    "userId" to userId
                )
                return mapper.writeValueAsString(errorResponse)
            }
            
            val response = mapOf(
                "success" to true,
                "profile" to mapOf(
                    "userId" to profile.userId,
                    "username" to profile.username,
                    "preferences" to mapOf(
                        "language" to profile.preferences.language,
                        "quality" to profile.preferences.quality,
                        "autoPlay" to profile.preferences.autoPlay,
                        "showSubtitles" to profile.preferences.showSubtitles,
                        "theme" to profile.preferences.theme,
                        "notifications" to profile.preferences.notifications,
                        "maxResultsPerPage" to profile.preferences.maxResultsPerPage,
                        "defaultSort" to profile.preferences.defaultSort
                    ),
                    "favoriteGenres" to profile.favoriteGenres,
                    "favoriteTypes" to profile.favoriteTypes.map { it.name },
                    "createdAt" to profile.createdAt,
                    "lastActive" to profile.lastActive
                )
            )
            
            mapper.writeValueAsString(response)
        } catch (e: Exception) {
            val errorResponse = mapOf(
                "success" to false,
                "error" to e.message,
                "userId" to userId
            )
            mapper.writeValueAsString(errorResponse)
        }
    }
    
    /**
     * Kullanıcı tercihlerini günceller
     */
    fun updateUserPreferencesJson(userId: String, preferences: Map<String, Any>): String {
        return try {
            val userPreferences = UserProfile.UserPreferences(
                language = preferences["language"] as? String ?: "tr",
                quality = preferences["quality"] as? String ?: "HD",
                autoPlay = preferences["autoPlay"] as? Boolean ?: false,
                showSubtitles = preferences["showSubtitles"] as? Boolean ?: true,
                theme = preferences["theme"] as? String ?: "dark",
                notifications = preferences["notifications"] as? Boolean ?: true,
                maxResultsPerPage = preferences["maxResultsPerPage"] as? Int ?: 20,
                defaultSort = preferences["defaultSort"] as? String ?: "RELEVANCE"
            )
            
            val success = UserProfile.updateUserPreferences(userId, userPreferences)
            
            val response = mapOf(
                "success" to success,
                "message" to if (success) "Tercihler güncellendi" else "Kullanıcı bulunamadı"
            )
            
            mapper.writeValueAsString(response)
        } catch (e: Exception) {
            val errorResponse = mapOf(
                "success" to false,
                "error" to e.message
            )
            mapper.writeValueAsString(errorResponse)
        }
    }
    
    /**
     * İzleme geçmişine ekler
     */
    fun addToWatchHistoryJson(userId: String, contentId: String, sourceName: String? = null): String {
        return try {
            val contentItem = unifiedPlugin.getContentById(contentId)
            
            if (contentItem == null) {
                val errorResponse = mapOf(
                    "success" to false,
                    "error" to "İçerik bulunamadı",
                    "contentId" to contentId
                )
                return mapper.writeValueAsString(errorResponse)
            }
            
            val success = UserProfile.addToWatchHistory(userId, contentItem, sourceName)
            
            val response = mapOf(
                "success" to success,
                "message" to if (success) "İzleme geçmişine eklendi" else "Kullanıcı bulunamadı"
            )
            
            mapper.writeValueAsString(response)
        } catch (e: Exception) {
            val errorResponse = mapOf(
                "success" to false,
                "error" to e.message
            )
            mapper.writeValueAsString(errorResponse)
        }
    }
    
    /**
     * Kullanıcı istatistiklerini döndürür
     */
    fun getUserStatsJson(userId: String): String {
        return try {
            val stats = UserProfile.getUserStats(userId)
            
            if (stats == null) {
                val errorResponse = mapOf(
                    "success" to false,
                    "error" to "Kullanıcı bulunamadı",
                    "userId" to userId
                )
                return mapper.writeValueAsString(errorResponse)
            }
            
            val response = mapOf(
                "success" to true,
                "stats" to mapOf(
                    "totalWatched" to stats.totalWatched,
                    "totalHours" to stats.totalHours,
                    "favoriteGenre" to stats.favoriteGenre,
                    "favoriteType" to stats.favoriteType?.name,
                    "mostWatchedSource" to stats.mostWatchedSource,
                    "averageRating" to stats.averageRating,
                    "completionRate" to stats.completionRate
                )
            )
            
            mapper.writeValueAsString(response)
        } catch (e: Exception) {
            val errorResponse = mapOf(
                "success" to false,
                "error" to e.message
            )
            mapper.writeValueAsString(errorResponse)
        }
    }
    
    /**
     * Kişiselleştirilmiş önerileri döndürür
     */
    fun getPersonalizedRecommendationsJson(userId: String, limit: Int = 20): String {
        return try {
            val allContent = unifiedPlugin.getAllContent()
            val recommendations = UserProfile.getPersonalizedRecommendations(userId, allContent, limit)
            
            val response = mapOf(
                "success" to true,
                "userId" to userId,
                "recommendations" to recommendations.map { it.toJsonMap() }
            )
            
            mapper.writeValueAsString(response)
        } catch (e: Exception) {
            val errorResponse = mapOf(
                "success" to false,
                "error" to e.message,
                "userId" to userId
            )
            mapper.writeValueAsString(errorResponse)
        }
    }
    
    /**
     * Kullanıcı aktivite geçmişini döndürür
     */
    fun getUserActivityHistoryJson(userId: String, days: Int = 30): String {
        return try {
            val history = UserProfile.getUserActivityHistory(userId, days)
            
            val response = mapOf(
                "success" to true,
                "userId" to userId,
                "days" to days,
                "history" to history.map { item ->
                    mapOf(
                        "contentId" to item.contentId,
                        "title" to item.title,
                        "type" to item.type.name,
                        "watchDate" to item.watchDate,
                        "watchDuration" to item.watchDuration,
                        "completed" to item.completed,
                        "rating" to item.rating,
                        "sourceName" to item.sourceName
                    )
                }
            )
            
            mapper.writeValueAsString(response)
        } catch (e: Exception) {
            val errorResponse = mapOf(
                "success" to false,
                "error" to e.message,
                "userId" to userId
            )
            mapper.writeValueAsString(errorResponse)
        }
    }
    
    /**
     * Kullanıcı bildirimlerini döndürür
     */
    fun getUserNotificationsJson(userId: String, limit: Int = 50): String {
        return try {
            val notifications = NotificationSystem.getUserNotifications(userId, limit)
            
            val response = mapOf(
                "success" to true,
                "userId" to userId,
                "notifications" to notifications.map { notification ->
                    mapOf(
                        "id" to notification.id,
                        "type" to notification.type.name,
                        "priority" to notification.priority.name,
                        "title" to notification.title,
                        "message" to notification.message,
                        "contentId" to notification.contentId,
                        "actionUrl" to notification.actionUrl,
                        "imageUrl" to notification.imageUrl,
                        "createdAt" to notification.createdAt,
                        "readAt" to notification.readAt,
                        "expiresAt" to notification.expiresAt
                    )
                }
            )
            
            mapper.writeValueAsString(response)
        } catch (e: Exception) {
            val errorResponse = mapOf(
                "success" to false,
                "error" to e.message,
                "userId" to userId
            )
            mapper.writeValueAsString(errorResponse)
        }
    }
    
    /**
     * Okunmamış bildirimleri döndürür
     */
    fun getUnreadNotificationsJson(userId: String): String {
        return try {
            val notifications = NotificationSystem.getUnreadNotifications(userId)
            
            val response = mapOf(
                "success" to true,
                "userId" to userId,
                "unreadCount" to notifications.size,
                "notifications" to notifications.map { notification ->
                    mapOf(
                        "id" to notification.id,
                        "type" to notification.type.name,
                        "priority" to notification.priority.name,
                        "title" to notification.title,
                        "message" to notification.message,
                        "contentId" to notification.contentId,
                        "actionUrl" to notification.actionUrl,
                        "createdAt" to notification.createdAt
                    )
                }
            )
            
            mapper.writeValueAsString(response)
        } catch (e: Exception) {
            val errorResponse = mapOf(
                "success" to false,
                "error" to e.message,
                "userId" to userId
            )
            mapper.writeValueAsString(errorResponse)
        }
    }
    
    /**
     * Bildirimi okundu olarak işaretler
     */
    fun markNotificationAsReadJson(notificationId: String): String {
        return try {
            val success = NotificationSystem.markAsRead(notificationId)
            
            val response = mapOf(
                "success" to success,
                "message" to if (success) "Bildirim okundu olarak işaretlendi" else "Bildirim bulunamadı"
            )
            
            mapper.writeValueAsString(response)
        } catch (e: Exception) {
            val errorResponse = mapOf(
                "success" to false,
                "error" to e.message
            )
            mapper.writeValueAsString(errorResponse)
        }
    }
    
    /**
     * Tüm bildirimleri okundu olarak işaretler
     */
    fun markAllNotificationsAsReadJson(userId: String): String {
        return try {
            val count = NotificationSystem.markAllAsRead(userId)
            
            val response = mapOf(
                "success" to true,
                "message" to "$count bildirim okundu olarak işaretlendi",
                "count" to count
            )
            
            mapper.writeValueAsString(response)
        } catch (e: Exception) {
            val errorResponse = mapOf(
                "success" to false,
                "error" to e.message
            )
            mapper.writeValueAsString(errorResponse)
        }
    }
    
    /**
     * Bildirim ayarlarını döndürür
     */
    fun getNotificationSettingsJson(userId: String): String {
        return try {
            val settings = NotificationSystem.getNotificationSettings(userId)
            
            if (settings == null) {
                val errorResponse = mapOf(
                    "success" to false,
                    "error" to "Bildirim ayarları bulunamadı",
                    "userId" to userId
                )
                return mapper.writeValueAsString(errorResponse)
            }
            
            val response = mapOf(
                "success" to true,
                "settings" to mapOf(
                    "userId" to settings.userId,
                    "enabled" to settings.enabled,
                    "newContent" to settings.newContent,
                    "recommendations" to settings.recommendations,
                    "systemUpdates" to settings.systemUpdates,
                    "maintenance" to settings.maintenance,
                    "promotions" to settings.promotions,
                    "personal" to settings.personal,
                    "quietHours" to settings.quietHours,
                    "quietHoursStart" to settings.quietHoursStart,
                    "quietHoursEnd" to settings.quietHoursEnd,
                    "maxNotifications" to settings.maxNotifications
                )
            )
            
            mapper.writeValueAsString(response)
        } catch (e: Exception) {
            val errorResponse = mapOf(
                "success" to false,
                "error" to e.message,
                "userId" to userId
            )
            mapper.writeValueAsString(errorResponse)
        }
    }
    
    /**
     * Bildirim ayarlarını günceller
     */
    fun updateNotificationSettingsJson(userId: String, settings: Map<String, Any>): String {
        return try {
            val notificationSettings = NotificationSystem.NotificationSettings(
                userId = userId,
                enabled = settings["enabled"] as? Boolean ?: true,
                newContent = settings["newContent"] as? Boolean ?: true,
                recommendations = settings["recommendations"] as? Boolean ?: true,
                systemUpdates = settings["systemUpdates"] as? Boolean ?: true,
                maintenance = settings["maintenance"] as? Boolean ?: true,
                promotions = settings["promotions"] as? Boolean ?: false,
                personal = settings["personal"] as? Boolean ?: true,
                quietHours = settings["quietHours"] as? Boolean ?: false,
                quietHoursStart = settings["quietHoursStart"] as? Int ?: 22,
                quietHoursEnd = settings["quietHoursEnd"] as? Int ?: 8,
                maxNotifications = settings["maxNotifications"] as? Int ?: 100
            )
            
            val success = NotificationSystem.updateNotificationSettings(userId, notificationSettings)
            
            val response = mapOf(
                "success" to success,
                "message" to if (success) "Bildirim ayarları güncellendi" else "Güncelleme başarısız"
            )
            
            mapper.writeValueAsString(response)
        } catch (e: Exception) {
            val errorResponse = mapOf(
                "success" to false,
                "error" to e.message
            )
            mapper.writeValueAsString(errorResponse)
        }
    }
    
    /**
     * Tür bazlı arama yapar
     */
    fun searchByTypeJson(query: String, type: String): String {
        return runBlocking {
            try {
                val tvType = when (type.lowercase()) {
                    "movie" -> com.lagradost.cloudstream3.TvType.Movie
                    "tvseries", "series" -> com.lagradost.cloudstream3.TvType.TvSeries
                    "anime" -> com.lagradost.cloudstream3.TvType.Anime
                    "documentary" -> com.lagradost.cloudstream3.TvType.Documentary
                    else -> null
                }
                
                if (tvType == null) {
                    val errorResponse = mapOf(
                        "success" to false,
                        "error" to "Geçersiz tür: $type",
                        "supportedTypes" to listOf("movie", "tvseries", "anime", "documentary")
                    )
                    return@runBlocking mapper.writeValueAsString(errorResponse)
                }
                
                val results = unifiedPlugin.searchByType(query, tvType)
                val contentItems = results.map { searchResponse ->
                    val contentId = searchResponse.url.removePrefix("unified://")
                    unifiedPlugin.getContentById(contentId) ?: createContentItemFromSearchResponse(searchResponse)
                }
                
                val response = mapOf(
                    "success" to true,
                    "query" to query,
                    "type" to type,
                    "totalResults" to contentItems.size,
                    "results" to contentItems.map { it.toJsonMap() }
                )
                
                mapper.writeValueAsString(response)
            } catch (e: Exception) {
                val errorResponse = mapOf(
                    "success" to false,
                    "error" to e.message,
                    "query" to query,
                    "type" to type
                )
                mapper.writeValueAsString(errorResponse)
            }
        }
    }
    
    /**
     * Kategori/tür bazlı arama yapar
     */
    fun searchByGenreJson(query: String, genre: String): String {
        return runBlocking {
            try {
                val results = unifiedPlugin.searchByGenre(query, genre)
                val contentItems = results.map { searchResponse ->
                    val contentId = searchResponse.url.removePrefix("unified://")
                    unifiedPlugin.getContentById(contentId) ?: createContentItemFromSearchResponse(searchResponse)
                }
                
                val response = mapOf(
                    "success" to true,
                    "query" to query,
                    "genre" to genre,
                    "totalResults" to contentItems.size,
                    "results" to contentItems.map { it.toJsonMap() }
                )
                
                mapper.writeValueAsString(response)
            } catch (e: Exception) {
                val errorResponse = mapOf(
                    "success" to false,
                    "error" to e.message,
                    "query" to query,
                    "genre" to genre
                )
                mapper.writeValueAsString(errorResponse)
            }
        }
    }
    
    /**
     * Cache'i temizler ve JSON yanıt döndürür
     */
    fun clearCacheJson(): String {
        return try {
            unifiedPlugin.clearCache()
            val response = mapOf(
                "success" to true,
                "message" to "Cache temizlendi"
            )
            mapper.writeValueAsString(response)
        } catch (e: Exception) {
            val errorResponse = mapOf(
                "success" to false,
                "error" to e.message
            )
            mapper.writeValueAsString(errorResponse)
        }
    }
    
    /**
     * ContentItem'ı JSON map'e çevirir
     */
    private fun ContentItem.toJsonMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "title" to title,
            "normalizedTitle" to normalizedTitle,
            "type" to type.name,
            "year" to year,
            "poster" to poster,
            "description" to description,
            "rating" to rating,
            "duration" to duration,
            "tags" to tags,
            "sources" to sources.map { it.toJsonMap() },
            "lastUpdated" to lastUpdated,
            "sourceCount" to sources.size
        )
    }
    
    /**
     * ContentSource'u JSON map'e çevirir
     */
    private fun ContentSource.toJsonMap(): Map<String, Any?> {
        return mapOf(
            "sourceName" to sourceName,
            "sourceUrl" to sourceUrl,
            "originalTitle" to originalTitle,
            "quality" to quality,
            "language" to language,
            "isAvailable" to isAvailable,
            "lastChecked" to lastChecked
        )
    }
    
    /**
     * SearchResponse'dan ContentItem oluşturur
     */
    private fun createContentItemFromSearchResponse(searchResponse: com.lagradost.cloudstream3.SearchResponse): ContentItem {
        val contentId = searchResponse.url.removePrefix("unified://")
        return ContentItem(
            id = contentId,
            title = searchResponse.name,
            normalizedTitle = ContentNormalizer.normalizeTitle(searchResponse.name),
            type = searchResponse.type,
            year = searchResponse.year,
            poster = searchResponse.posterUrl
        )
    }
} 