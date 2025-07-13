package com.keyiflerolsun

import android.util.Log
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer

/**
 * Tüm kaynakları birleştiren ana eklenti sınıfı
 */
class UnifiedSearchPlugin : MainAPI() {
    
    companion object {
        private const val TAG = "UnifiedSearchPlugin"
    }
    
    override var mainUrl = "https://unified.kekik-reco.com"
    override var name = "Kekik Reco - Birleşik Arama"
    override val hasMainPage = true
    override var lang = "tr"
    override val hasQuickSearch = true
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries, TvType.Anime)
    
    // Ana sayfa kategorileri
    override val mainPage = mainPageOf(
        "trending" to "Trend İçerikler",
        "recent" to "Yeni Çıkanlar",
        "top_rated" to "En Yüksek Puanlılar",
        "multi_source" to "Çoklu Kaynak",
        "movies" to "Filmler",
        "tv_series" to "Diziler",
        "anime" to "Animeler",
        "action" to "Aksiyon",
        "drama" to "Dram",
        "comedy" to "Komedi",
        "thriller" to "Gerilim",
        "sci_fi" to "Bilim Kurgu",
        "horror" to "Korku",
        "romance" to "Romantik",
        "documentary" to "Belgeseller",
        "this_year" to "Bu Yıl",
        "last_year" to "Geçen Yıl"
    )
    
    // İçerik havuzu yöneticisi
    private val contentPoolManager = ContentPoolManager()
    
    // Kaynak listesi - bu kısım runtime'da doldurulacak
    private val availableSources = mapOf(
        "DiziBox" to "DiziBox",
        "JetFilmizle" to "JetFilmizle", 
        "Filmİzlesene" to "Filmİzlesene",
        "HDFilmIzle" to "HDFilmIzle",
        "DiziPal" to "DiziPal",
        "DiziMore" to "DiziMore",
        "DiziYo" to "DiziYo",
        "DiziYou" to "DiziYou",
        "DiziMom" to "DiziMom",
        "DiziKorea" to "DiziKorea",
        "DiziGom" to "DiziGom",
        "DiziFun" to "DiziFun",
        "Ddizi" to "Ddizi",
        "Dizilla" to "Dizilla",
        "DiziMag" to "DiziMag",
        "RoketDizi" to "RoketDizi",
        "TrDiziIzle" to "TrDiziIzle",
        "TvDiziler" to "TvDiziler",
        "YabanciDizi" to "YabanciDizi",
        "KoreanTurk" to "KoreanTurk",
        "AnimeciX" to "AnimeciX",
        "AnimeIzlesene" to "AnimeIzlesene",
        "Animeler" to "Animeler",
        "Animex" to "Animex",
        "Anizm" to "Anizm",
        "AsyaAnimeleri" to "AsyaAnimeleri",
        "AsyaWatch" to "AsyaWatch",
        "TurkAnime" to "TurkAnime",
        "TRanimaci" to "TRanimaci",
        "Donghuastream" to "Donghuastream",
        "DragonTV" to "DragonTV",
        "CizgiMax" to "CizgiMax",
        "CizgiveDizi" to "CizgiveDizi",
        "TRasyalog" to "TRasyalog",
        "TLCtr" to "TLCtr",
        "BelgeselX" to "BelgeselX",
        "CanliTV" to "CanliTV",
        "CanliTelevizyon" to "CanliTelevizyon",
        "GinikoCanli" to "GinikoCanli",
        "IpTvPlayStream" to "IpTvPlayStream",
        "iptvSevenler" to "iptvSevenler",
        "RecTV" to "RecTV",
        "Vavoo" to "Vavoo",
        "vavooSpor" to "vavooSpor",
        "Tafdi" to "Tafdi",
        "OxAx" to "OxAx",
        "InatBox" to "InatBox",
        "SelcukFlix" to "SelcukFlix",
        "SetFilmIzle" to "SetFilmIzle",
        "SezonlukDizi" to "SezonlukDizi",
        "Sinefy" to "Sinefy",
        "SinemaCX" to "SinemaCX",
        "SineWix" to "SineWix",
        "SuperFilmGeldi" to "SuperFilmGeldi",
        "UgurFilm" to "UgurFilm",
        "Watch2Movies" to "Watch2Movies",
        "Watch32" to "Watch32",
        "WebteIzle" to "WebteIzle",
        "WFilmİzle" to "WFilmİzle",
        "YTS" to "YTS",
        "4KFilmIzlesene" to "4KFilmIzlesene",
        "FilmBip" to "FilmBip",
        "FilmKovasi" to "FilmKovasi",
        "FilmMakinesi" to "FilmMakinesi",
        "FilmModu" to "FilmModu",
        "FullHDFilm" to "FullHDFilm",
        "FullHDFilmİzlede" to "FullHDFilmİzlede",
        "FullHDFilmizlesene" to "FullHDFilmizlesene",
        "HDFilmCehennemi" to "HDFilmCehennemi",
        "HDFilmCehennemi2" to "HDFilmCehennemi2",
        "HDFilmSitesi" to "HDFilmSitesi",
        "JetFilmizle" to "JetFilmizle",
        "KultFilmler" to "KultFilmler",
        "NetflixMirror" to "NetflixMirror",
        "powerDizi" to "powerDizi",
        "powerSinema" to "powerSinema",
        "RareFilmm" to "RareFilmm",
        "SetFilmIzle" to "SetFilmIzle",
        "SpankBang" to "SpankBang",
        "PornHub" to "PornHub",
        "xHamster" to "xHamster",
        "HQPorner" to "HQPorner",
        "FullPorner" to "FullPorner",
        "UncutMaza" to "UncutMaza",
        "YouTube" to "YouTube"
    )
    
    init {
        // Kaynakları yöneticiye ekle
        initializeSources()
    }
    
    /**
     * Kaynakları başlatır
     */
    private fun initializeSources() {
        Log.d(TAG, "Kaynaklar başlatılıyor...")
        
        // Bu kısım runtime'da Cloudstream tarafından doldurulacak
        // Şimdilik boş bırakıyoruz
    }
    
    /**
     * Kaynak ekler (runtime'da çağrılacak)
     */
    fun addSource(sourceName: String, source: MainAPI) {
        contentPoolManager.addSource(sourceName, source)
        Log.d(TAG, "Kaynak eklendi: $sourceName")
    }
    
    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        Log.d(TAG, "Ana sayfa kategorisi yükleniyor: ${request.name}")
        
        val allContent = contentPoolManager.getAllContent()
        val categories = MainPageCategories.getMainPageCategories()
        val category = categories.find { it.id == request.data }
        
        if (category != null) {
            val categoryContent = MainPageCategories.getCategoryContent(category, allContent, 20)
            val searchResponses = categoryContent.map { it.toSearchResponse() }
            
            Log.d(TAG, "Kategori içerikleri yüklendi: ${searchResponses.size} sonuç")
            return newHomePageResponse(request.name, searchResponses)
        }
        
        // Varsayılan olarak trend içerikleri döndür
        val trendingContent = MainPageCategories.getCategoryContent(
            categories.first { it.id == "trending" }, 
            allContent, 
            20
        )
        val searchResponses = trendingContent.map { it.toSearchResponse() }
        
        return newHomePageResponse(request.name, searchResponses)
    }
    
    override suspend fun search(query: String): List<SearchResponse> {
        Log.d(TAG, "Birleşik arama başlatılıyor: $query")
        
        // Cache'den kontrol et
        val cachedContent = contentPoolManager.getCachedContent(query)
        if (cachedContent != null) {
            Log.d(TAG, "Cache'den ${cachedContent.size} sonuç döndürülüyor")
            return cachedContent.map { it.toSearchResponse() }
        }
        
        // Tüm kaynaklardan arama yap
        val mergedContent = contentPoolManager.searchAndMerge(query)
        
        // Sonuçları SearchResponse formatına çevir
        val searchResponses = mergedContent.map { it.toSearchResponse() }
        
        Log.d(TAG, "Birleşik arama tamamlandı: ${searchResponses.size} sonuç")
        return searchResponses
    }
    
    override suspend fun quickSearch(query: String): List<SearchResponse> = search(query)
    
    override suspend fun load(url: String): LoadResponse? {
        // URL formatı: "unified://contentId"
        val contentId = url.removePrefix("unified://")
        
        // İçerik havuzundan bul
        val contentItem = contentPoolManager.getContentById(contentId)
        if (contentItem == null) {
            Log.e(TAG, "İçerik bulunamadı: $contentId")
            return null
        }
        
        // İlk kaynaktan detay bilgilerini al
        val firstSource = contentItem.sources.firstOrNull()
        if (firstSource == null) {
            Log.e(TAG, "Kaynak bulunamadı: $contentId")
            return null
        }
        
        // Kaynak eklentisinden detay bilgilerini al
        val sourcePlugin = contentPoolManager.getSourcePlugin(firstSource.sourceName)
        if (sourcePlugin == null) {
            Log.e(TAG, "Kaynak eklentisi bulunamadı: ${firstSource.sourceName}")
            return null
        }
        
        try {
            val loadResponse = sourcePlugin.load(firstSource.sourceUrl)
            if (loadResponse != null) {
                // Kaynak listesini ekle
                return loadResponse.copy(
                    data = url, // Unified URL'i kullan
                    name = contentItem.title,
                    posterUrl = contentItem.poster ?: loadResponse.posterUrl,
                    year = contentItem.year ?: loadResponse.year,
                    plot = contentItem.description ?: loadResponse.plot,
                    rating = contentItem.rating ?: loadResponse.rating,
                    duration = contentItem.duration ?: loadResponse.duration,
                    tags = contentItem.tags.ifEmpty { loadResponse.tags }
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Kaynak yükleme hatası: ${e.message}")
        }
        
        return null
    }
    
    override suspend fun loadLinks(
        data: String, 
        isCasting: Boolean, 
        subtitleCallback: (SubtitleFile) -> Unit, 
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        // URL formatı: "unified://contentId"
        val contentId = data.removePrefix("unified://")
        
        // İçerik havuzundan bul
        val contentItem = contentPoolManager.getContentById(contentId)
        if (contentItem == null) {
            Log.e(TAG, "İçerik bulunamadı: $contentId")
            return false
        }
        
        // Tüm kaynaklardan link yükle
        var success = false
        contentItem.sources.forEach { source ->
            try {
                val sourcePlugin = contentPoolManager.getSourcePlugin(source.sourceName)
                if (sourcePlugin != null) {
                    val result = sourcePlugin.loadLinks(
                        source.sourceUrl, 
                        isCasting, 
                        subtitleCallback, 
                        callback
                    )
                    if (result) success = true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Kaynak link yükleme hatası (${source.sourceName}): ${e.message}")
            }
        }
        
        return success
    }
    
    /**
     * ContentItem'ı SearchResponse'a çevirir
     */
    private fun ContentItem.toSearchResponse(): SearchResponse {
        val unifiedUrl = "unified://$id"
        
        return when (type) {
            TvType.Movie -> newMovieSearchResponse(title, unifiedUrl, type) {
                this.posterUrl = poster
                this.year = year
            }
            TvType.TvSeries -> newTvSeriesSearchResponse(title, unifiedUrl, type) {
                this.posterUrl = poster
                this.year = year
            }
            TvType.Anime -> newAnimeSearchResponse(title, unifiedUrl, type) {
                this.posterUrl = poster
                this.year = year
            }
            else -> newMovieSearchResponse(title, unifiedUrl, TvType.Movie) {
                this.posterUrl = poster
                this.year = year
            }
        }
    }
    
    /**
     * İstatistikleri döndürür
     */
    fun getStats(): Map<String, Any> = contentPoolManager.getStats()
    
    /**
     * Tür istatistiklerini döndürür
     */
    fun getTypeStats(): Map<String, Int> = contentPoolManager.getTypeStats()
    
    /**
     * Kategori istatistiklerini döndürür
     */
    fun getGenreStats(): Map<String, Int> = contentPoolManager.getGenreStats()
    
    /**
     * Yıl istatistiklerini döndürür
     */
    fun getYearStats(): Map<Int, Int> = contentPoolManager.getYearStats()
    
    /**
     * Tüm içerikleri döndürür
     */
    fun getAllContent(): List<ContentItem> = contentPoolManager.getAllContent()
    
    /**
     * ID'ye göre içerik alır
     */
    fun getContentById(contentId: String): ContentItem? {
        return contentPoolManager.getContentById(contentId)
    }
    
    /**
     * Tür bazlı arama yapar
     */
    suspend fun searchByType(query: String, type: TvType): List<SearchResponse> {
        Log.d(TAG, "Tür bazlı arama başlatılıyor: $query (Tür: $type)")
        
        val allResults = search(query)
        val filteredResults = allResults.filter { it.type == type }
        
        Log.d(TAG, "Tür bazlı arama tamamlandı: ${filteredResults.size} sonuç")
        return filteredResults
    }
    
    /**
     * Kategori/tür bazlı arama yapar
     */
    suspend fun searchByGenre(query: String, genre: String): List<SearchResponse> {
        Log.d(TAG, "Kategori bazlı arama başlatılıyor: $query (Kategori: $genre)")
        
        val allResults = search(query)
        val filteredResults = allResults.filter { searchResponse ->
            val contentId = searchResponse.url.removePrefix("unified://")
            val contentItem = contentPoolManager.getContentById(contentId)
            contentItem?.tags?.any { it.contains(genre, ignoreCase = true) } == true
        }
        
        Log.d(TAG, "Kategori bazlı arama tamamlandı: ${filteredResults.size} sonuç")
        return filteredResults
    }
    
    /**
     * Cache'i temizler
     */
    fun clearCache() = contentPoolManager.clearCache()
} 