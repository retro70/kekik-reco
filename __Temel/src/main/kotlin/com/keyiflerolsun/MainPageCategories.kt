package com.keyiflerolsun

import android.util.Log
import com.lagradost.cloudstream3.*

/**
 * Ana sayfa kategorileri ve özel koleksiyonlar sistemi
 */
object MainPageCategories {
    
    private const val TAG = "MainPageCategories"
    
    /**
     * Ana sayfa kategorileri
     */
    fun getMainPageCategories(): List<MainPageCategory> {
        return listOf(
            MainPageCategory(
                id = "trending",
                title = "Trend İçerikler",
                description = "En popüler ve güncel içerikler",
                type = CategoryType.TRENDING
            ),
            MainPageCategory(
                id = "recent",
                title = "Yeni Çıkanlar",
                description = "Son eklenen içerikler",
                type = CategoryType.RECENT
            ),
            MainPageCategory(
                id = "top_rated",
                title = "En Yüksek Puanlılar",
                description = "Kullanıcılar tarafından en çok beğenilenler",
                type = CategoryType.TOP_RATED
            ),
            MainPageCategory(
                id = "multi_source",
                title = "Çoklu Kaynak",
                description = "Birden fazla kaynakta bulunan içerikler",
                type = CategoryType.MULTI_SOURCE
            ),
            MainPageCategory(
                id = "movies",
                title = "Filmler",
                description = "En iyi filmler",
                type = CategoryType.TYPE_BASED,
                filterType = TvType.Movie
            ),
            MainPageCategory(
                id = "tv_series",
                title = "Diziler",
                description = "En iyi diziler",
                type = CategoryType.TYPE_BASED,
                filterType = TvType.TvSeries
            ),
            MainPageCategory(
                id = "anime",
                title = "Animeler",
                description = "En iyi animeler",
                type = CategoryType.TYPE_BASED,
                filterType = TvType.Anime
            ),
            MainPageCategory(
                id = "action",
                title = "Aksiyon",
                description = "Aksiyon türündeki içerikler",
                type = CategoryType.GENRE_BASED,
                filterGenre = "Aksiyon"
            ),
            MainPageCategory(
                id = "drama",
                title = "Dram",
                description = "Dram türündeki içerikler",
                type = CategoryType.GENRE_BASED,
                filterGenre = "Dram"
            ),
            MainPageCategory(
                id = "comedy",
                title = "Komedi",
                description = "Komedi türündeki içerikler",
                type = CategoryType.GENRE_BASED,
                filterGenre = "Komedi"
            ),
            MainPageCategory(
                id = "thriller",
                title = "Gerilim",
                description = "Gerilim türündeki içerikler",
                type = CategoryType.GENRE_BASED,
                filterGenre = "Gerilim"
            ),
            MainPageCategory(
                id = "sci_fi",
                title = "Bilim Kurgu",
                description = "Bilim kurgu türündeki içerikler",
                type = CategoryType.GENRE_BASED,
                filterGenre = "Bilimkurgu"
            ),
            MainPageCategory(
                id = "horror",
                title = "Korku",
                description = "Korku türündeki içerikler",
                type = CategoryType.GENRE_BASED,
                filterGenre = "Korku"
            ),
            MainPageCategory(
                id = "romance",
                title = "Romantik",
                description = "Romantik türündeki içerikler",
                type = CategoryType.GENRE_BASED,
                filterGenre = "Romantik"
            ),
            MainPageCategory(
                id = "documentary",
                title = "Belgeseller",
                description = "En iyi belgeseller",
                type = CategoryType.TYPE_BASED,
                filterType = TvType.Documentary
            ),
            MainPageCategory(
                id = "this_year",
                title = "Bu Yıl",
                description = "Bu yıl çıkan içerikler",
                type = CategoryType.YEAR_BASED,
                filterYear = java.time.LocalDate.now().year
            ),
            MainPageCategory(
                id = "last_year",
                title = "Geçen Yıl",
                description = "Geçen yıl çıkan içerikler",
                type = CategoryType.YEAR_BASED,
                filterYear = java.time.LocalDate.now().year - 1
            )
        )
    }
    
    /**
     * Kategoriye göre içerikleri getirir
     */
    fun getCategoryContent(category: MainPageCategory, allContent: List<ContentItem>, limit: Int = 20): List<ContentItem> {
        Log.d(TAG, "Kategori içerikleri getiriliyor: ${category.title}")
        
        return when (category.type) {
            CategoryType.TRENDING -> getTrendingContent(allContent, limit)
            CategoryType.RECENT -> ContentRecommendation.getRecentContent(allContent, limit)
            CategoryType.TOP_RATED -> ContentRecommendation.getTopRatedContent(allContent, limit)
            CategoryType.MULTI_SOURCE -> ContentRecommendation.getMultiSourceContent(allContent, limit)
            CategoryType.TYPE_BASED -> getTypeBasedContent(allContent, category.filterType, limit)
            CategoryType.GENRE_BASED -> getGenreBasedContent(allContent, category.filterGenre, limit)
            CategoryType.YEAR_BASED -> getYearBasedContent(allContent, category.filterYear, limit)
        }
    }
    
    /**
     * Trend içerikleri getirir (karma algoritma)
     */
    private fun getTrendingContent(allContent: List<ContentItem>, limit: Int): List<ContentItem> {
        val trendingContent = mutableListOf<ContentItem>()
        
        // Yeni çıkanlar (%40)
        val recentCount = (limit * 0.4).toInt()
        val recent = ContentRecommendation.getRecentContent(allContent, recentCount)
        trendingContent.addAll(recent)
        
        // Yüksek puanlılar (%30)
        val topRatedCount = (limit * 0.3).toInt()
        val topRated = ContentRecommendation.getTopRatedContent(allContent, topRatedCount)
        trendingContent.addAll(topRated.filter { it !in trendingContent })
        
        // Çoklu kaynaklılar (%30)
        val remainingCount = limit - trendingContent.size
        if (remainingCount > 0) {
            val multiSource = ContentRecommendation.getMultiSourceContent(allContent, remainingCount)
            trendingContent.addAll(multiSource.filter { it !in trendingContent })
        }
        
        return trendingContent.take(limit)
    }
    
    /**
     * Tür bazlı içerikleri getirir
     */
    private fun getTypeBasedContent(allContent: List<ContentItem>, type: TvType?, limit: Int): List<ContentItem> {
        if (type == null) return emptyList()
        
        return allContent
            .filter { it.type == type }
            .sortedByDescending { it.rating ?: 0 }
            .take(limit)
    }
    
    /**
     * Kategori bazlı içerikleri getirir
     */
    private fun getGenreBasedContent(allContent: List<ContentItem>, genre: String?, limit: Int): List<ContentItem> {
        if (genre == null) return emptyList()
        
        return allContent
            .filter { content ->
                content.tags.any { it.contains(genre, ignoreCase = true) }
            }
            .sortedByDescending { it.rating ?: 0 }
            .take(limit)
    }
    
    /**
     * Yıl bazlı içerikleri getirir
     */
    private fun getYearBasedContent(allContent: List<ContentItem>, year: Int?, limit: Int): List<ContentItem> {
        if (year == null) return emptyList()
        
        return allContent
            .filter { it.year == year }
            .sortedByDescending { it.rating ?: 0 }
            .take(limit)
    }
    
    /**
     * Özel koleksiyonlar
     */
    fun getSpecialCollections(): List<SpecialCollection> {
        return listOf(
            SpecialCollection(
                id = "netflix_originals",
                title = "Netflix Orijinalleri",
                description = "Netflix tarafından üretilen içerikler",
                keywords = listOf("netflix", "original", "exclusive")
            ),
            SpecialCollection(
                id = "marvel_universe",
                title = "Marvel Evreni",
                description = "Marvel filmleri ve dizileri",
                keywords = listOf("marvel", "avengers", "spider-man", "iron man", "captain america")
            ),
            SpecialCollection(
                id = "star_wars",
                title = "Star Wars",
                description = "Star Wars filmleri ve dizileri",
                keywords = listOf("star wars", "jedi", "sith", "force")
            ),
            SpecialCollection(
                id = "game_of_thrones",
                title = "Game of Thrones",
                description = "Game of Thrones evreni",
                keywords = listOf("game of thrones", "got", "westeros", "dragon")
            ),
            SpecialCollection(
                id = "breaking_bad_universe",
                title = "Breaking Bad Evreni",
                description = "Breaking Bad ve Better Call Saul",
                keywords = listOf("breaking bad", "better call saul", "walter white", "saul goodman")
            )
        )
    }
    
    /**
     * Özel koleksiyon içeriklerini getirir
     */
    fun getSpecialCollectionContent(collection: SpecialCollection, allContent: List<ContentItem>, limit: Int = 20): List<ContentItem> {
        return allContent
            .filter { content ->
                collection.keywords.any { keyword ->
                    content.title.contains(keyword, ignoreCase = true) ||
                    content.tags.any { tag -> tag.contains(keyword, ignoreCase = true) }
                }
            }
            .sortedByDescending { it.rating ?: 0 }
            .take(limit)
    }
}

/**
 * Ana sayfa kategorisi
 */
data class MainPageCategory(
    val id: String,
    val title: String,
    val description: String,
    val type: CategoryType,
    val filterType: TvType? = null,
    val filterGenre: String? = null,
    val filterYear: Int? = null
)

/**
 * Kategori türü
 */
enum class CategoryType {
    TRENDING,       // Trend içerikler
    RECENT,         // Yeni çıkanlar
    TOP_RATED,      // En yüksek puanlılar
    MULTI_SOURCE,   // Çoklu kaynak
    TYPE_BASED,     // Tür bazlı (film, dizi, anime)
    GENRE_BASED,    // Kategori bazlı (aksiyon, dram)
    YEAR_BASED      // Yıl bazlı
}

/**
 * Özel koleksiyon
 */
data class SpecialCollection(
    val id: String,
    val title: String,
    val description: String,
    val keywords: List<String>
) 