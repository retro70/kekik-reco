package com.keyiflerolsun

import android.util.Log
import com.lagradost.cloudstream3.*
import kotlinx.coroutines.runBlocking

/**
 * İçerik havuzu sisteminin kullanım örnekleri
 */
object ContentPoolExample {
    
    private const val TAG = "ContentPoolExample"
    
    /**
     * Temel kullanım örneği
     */
    fun basicUsageExample() {
        Log.d(TAG, "=== Temel Kullanım Örneği ===")
        
        // 1. İçerik havuzu yöneticisini oluştur
        val contentPoolManager = ContentPoolManager()
        
        // 2. Kaynakları ekle (örnek)
        // contentPoolManager.addSource("DiziBox", DiziBox())
        // contentPoolManager.addSource("JetFilmizle", JetFilmizle())
        
        // 3. Arama yap ve sonuçları birleştir
        runBlocking {
            val query = "Breaking Bad"
            val results = contentPoolManager.searchAndMerge(query)
            
            Log.d(TAG, "Arama sonucu: ${results.size} benzersiz içerik")
            
            results.forEach { contentItem ->
                Log.d(TAG, "İçerik: ${contentItem.title}")
                Log.d(TAG, "  - Kaynak sayısı: ${contentItem.sources.size}")
                Log.d(TAG, "  - Kaynaklar: ${contentItem.sources.map { it.sourceName }}")
                Log.d(TAG, "  - Yıl: ${contentItem.year}")
                Log.d(TAG, "  - Tür: ${contentItem.type}")
            }
        }
    }
    
    /**
     * Normalizasyon örnekleri
     */
    fun normalizationExamples() {
        Log.d(TAG, "=== Normalizasyon Örnekleri ===")
        
        val titles = listOf(
            "Breaking Bad (2008) HD Türkçe Dublaj",
            "Breaking Bad 2008 Full HD",
            "Breaking Bad - 1. Sezon",
            "Breaking Bad Season 1",
            "Breaking Bad Türkçe Altyazılı",
            "Breaking Bad 1080p"
        )
        
        titles.forEach { title ->
            val normalized = ContentNormalizer.normalizeTitle(title)
            val id = ContentNormalizer.generateId(title)
            val year = ContentNormalizer.extractYear(title)
            val quality = ContentNormalizer.extractQuality(title)
            
            Log.d(TAG, "Orijinal: $title")
            Log.d(TAG, "  Normalize: $normalized")
            Log.d(TAG, "  ID: $id")
            Log.d(TAG, "  Yıl: $year")
            Log.d(TAG, "  Kalite: $quality")
            Log.d(TAG, "---")
        }
    }
    
    /**
     * Benzerlik hesaplama örnekleri
     */
    fun similarityExamples() {
        Log.d(TAG, "=== Benzerlik Hesaplama Örnekleri ===")
        
        val titlePairs = listOf(
            Pair("Breaking Bad (2008)", "Breaking Bad 2008"),
            Pair("Breaking Bad HD", "Breaking Bad Full HD"),
            Pair("Breaking Bad Türkçe", "Breaking Bad English"),
            Pair("Breaking Bad", "Better Call Saul"),
            Pair("Breaking Bad Season 1", "Breaking Bad 1. Sezon")
        )
        
        titlePairs.forEach { (title1, title2) ->
            val similarity = ContentNormalizer.calculateSimilarity(title1, title2)
            val isMatch = ContentNormalizer.isMatch(title1, title2)
            
            Log.d(TAG, "Karşılaştırma:")
            Log.d(TAG, "  '$title1' vs '$title2'")
            Log.d(TAG, "  Benzerlik: ${String.format("%.2f", similarity)}")
            Log.d(TAG, "  Eşleşme: $isMatch")
            Log.d(TAG, "---")
        }
    }
    
    /**
     * JSON API kullanım örneği
     */
    fun jsonApiExample() {
        Log.d(TAG, "=== JSON API Kullanım Örneği ===")
        
        // Arama sonuçlarını JSON formatında al
        val searchJson = JsonApi.searchJson("Breaking Bad")
        Log.d(TAG, "Arama JSON: $searchJson")
        
        // Tür bazlı arama
        val typeSearchJson = JsonApi.searchByTypeJson("Breaking Bad", "tvseries")
        Log.d(TAG, "Tür bazlı arama JSON: $typeSearchJson")
        
        // Kategori bazlı arama
        val genreSearchJson = JsonApi.searchByGenreJson("Breaking Bad", "Dram")
        Log.d(TAG, "Kategori bazlı arama JSON: $genreSearchJson")
        
        // İstatistikleri al
        val statsJson = JsonApi.getStatsJson()
        Log.d(TAG, "İstatistikler JSON: $statsJson")
        
        // Tür istatistikleri
        val typeStatsJson = JsonApi.getTypeStatsJson()
        Log.d(TAG, "Tür istatistikleri JSON: $typeStatsJson")
        
        // Kategori istatistikleri
        val genreStatsJson = JsonApi.getGenreStatsJson()
        Log.d(TAG, "Kategori istatistikleri JSON: $genreStatsJson")
        
        // Popüler türler
        val popularGenresJson = JsonApi.getPopularGenresJson()
        Log.d(TAG, "Popüler türler JSON: $popularGenresJson")
        
        // Karma öneriler
        val mixedRecommendationsJson = JsonApi.getMixedRecommendationsJson()
        Log.d(TAG, "Karma öneriler JSON: $mixedRecommendationsJson")
    }
    
    /**
     * Performans testi
     */
    fun performanceTest() {
        Log.d(TAG, "=== Performans Testi ===")
        
        val queries = listOf(
            "Breaking Bad",
            "Game of Thrones", 
            "The Walking Dead",
            "Stranger Things",
            "Money Heist"
        )
        
        val contentPoolManager = ContentPoolManager()
        
        runBlocking {
            val startTime = System.currentTimeMillis()
            
            queries.forEach { query ->
                val queryStartTime = System.currentTimeMillis()
                val results = contentPoolManager.searchAndMerge(query)
                val queryEndTime = System.currentTimeMillis()
                
                Log.d(TAG, "Sorgu: $query")
                Log.d(TAG, "  Sonuç sayısı: ${results.size}")
                Log.d(TAG, "  Süre: ${queryEndTime - queryStartTime}ms")
            }
            
            val endTime = System.currentTimeMillis()
            Log.d(TAG, "Toplam süre: ${endTime - startTime}ms")
        }
    }
    
    /**
     * Öneriler sistemi testi
     */
    fun recommendationTest() {
        Log.d(TAG, "=== Öneriler Sistemi Testi ===")
        
        // Örnek içerikler oluştur
        val sampleContent = listOf(
            ContentItem(
                id = "breaking_bad",
                title = "Breaking Bad",
                normalizedTitle = "breaking bad",
                type = TvType.TvSeries,
                year = 2008,
                tags = listOf("Dram", "Suç", "Gerilim")
            ),
            ContentItem(
                id = "better_call_saul",
                title = "Better Call Saul",
                normalizedTitle = "better call saul",
                type = TvType.TvSeries,
                year = 2015,
                tags = listOf("Dram", "Suç", "Gerilim")
            ),
            ContentItem(
                id = "game_of_thrones",
                title = "Game of Thrones",
                normalizedTitle = "game of thrones",
                type = TvType.TvSeries,
                year = 2011,
                tags = listOf("Dram", "Fantastik", "Macera")
            )
        )
        
        // İçerik önerileri testi
        val recommendations = ContentRecommendation.getContentRecommendations(
            sampleContent[0], sampleContent, 5
        )
        
        Log.d(TAG, "Breaking Bad için öneriler:")
        recommendations.forEach { content ->
            Log.d(TAG, "  - ${content.title} (${content.year})")
        }
        
        // Popüler türler testi
        val popularGenres = ContentRecommendation.getPopularGenres(sampleContent, 5)
        Log.d(TAG, "Popüler türler: $popularGenres")
        
        // Karma öneriler testi
        val mixedRecommendations = ContentRecommendation.getMixedRecommendations(sampleContent, 5)
        Log.d(TAG, "Karma öneriler: ${mixedRecommendations.size} içerik")
    }
    
    /**
     * Tür bazlı arama testi
     */
    fun typeBasedSearchTest() {
        Log.d(TAG, "=== Tür Bazlı Arama Testi ===")
        
        val contentPoolManager = ContentPoolManager()
        
        runBlocking {
            // Film arama
            val movieResults = contentPoolManager.searchAndMerge("Avengers")
                .filter { it.type == TvType.Movie }
            Log.d(TAG, "Film sonuçları: ${movieResults.size}")
            
            // Dizi arama
            val seriesResults = contentPoolManager.searchAndMerge("Breaking Bad")
                .filter { it.type == TvType.TvSeries }
            Log.d(TAG, "Dizi sonuçları: ${seriesResults.size}")
            
            // Anime arama
            val animeResults = contentPoolManager.searchAndMerge("Naruto")
                .filter { it.type == TvType.Anime }
            Log.d(TAG, "Anime sonuçları: ${animeResults.size}")
        }
    }
    
    /**
     * Ana sayfa kategorileri testi
     */
    fun mainPageCategoriesTest() {
        Log.d(TAG, "=== Ana Sayfa Kategorileri Testi ===")
        
        val categories = MainPageCategories.getMainPageCategories()
        Log.d(TAG, "Toplam kategori sayısı: ${categories.size}")
        
        categories.forEach { category ->
            Log.d(TAG, "Kategori: ${category.title} (${category.type})")
        }
        
        // Özel koleksiyonlar
        val collections = MainPageCategories.getSpecialCollections()
        Log.d(TAG, "Özel koleksiyon sayısı: ${collections.size}")
        
        collections.forEach { collection ->
            Log.d(TAG, "Koleksiyon: ${collection.title}")
        }
    }
    
    /**
     * Gelişmiş arama testi
     */
    fun advancedSearchTest() {
        Log.d(TAG, "=== Gelişmiş Arama Testi ===")
        
        val sampleContent = listOf(
            ContentItem(
                id = "breaking_bad",
                title = "Breaking Bad",
                normalizedTitle = "breaking bad",
                type = TvType.TvSeries,
                year = 2008,
                rating = 9,
                tags = listOf("Dram", "Suç", "Gerilim"),
                sources = listOf(
                    ContentSource("DiziBox", "url1", "Breaking Bad", "HD", "tr"),
                    ContentSource("JetFilmizle", "url2", "Breaking Bad", "Full HD", "tr")
                )
            ),
            ContentItem(
                id = "game_of_thrones",
                title = "Game of Thrones",
                normalizedTitle = "game of thrones",
                type = TvType.TvSeries,
                year = 2011,
                rating = 9,
                tags = listOf("Dram", "Fantastik", "Macera"),
                sources = listOf(ContentSource("DiziBox", "url3", "Game of Thrones", "HD", "tr"))
            )
        )
        
        // Filtre testi
        val filter = AdvancedSearch.SearchFilter(
            query = "Breaking",
            type = TvType.TvSeries,
            genres = listOf("Dram"),
            yearFrom = 2000,
            yearTo = 2010,
            ratingFrom = 8,
            minSources = 2
        )
        
        val result = AdvancedSearch.advancedSearch(
            allContent = sampleContent,
            filter = filter,
            sortOption = AdvancedSearch.SortOption.RATING_DESC
        )
        
        Log.d(TAG, "Gelişmiş arama sonucu: ${result.content.size} içerik")
        Log.d(TAG, "Toplam sayfa: ${result.totalPages}")
        
        // Otomatik tamamlama testi
        val suggestions = AdvancedSearch.getAutoCompleteSuggestions("Break", sampleContent, 5)
        Log.d(TAG, "Otomatik tamamlama önerileri: $suggestions")
        
        // Popüler aramalar testi
        val popularSearches = AdvancedSearch.getPopularSearches(sampleContent, 5)
        Log.d(TAG, "Popüler aramalar: $popularSearches")
    }
    
    /**
     * Kullanıcı profili testi
     */
    fun userProfileTest() {
        Log.d(TAG, "=== Kullanıcı Profili Testi ===")
        
        val userId = "test_user_123"
        val username = "TestUser"
        
        // Kullanıcı profili oluştur
        val profile = UserProfile.createUserProfile(userId, username)
        Log.d(TAG, "Kullanıcı profili oluşturuldu: ${profile.username}")
        
        // Favori kategoriler ekle
        UserProfile.updateFavoriteGenres(userId, listOf("Dram", "Aksiyon", "Komedi"))
        Log.d(TAG, "Favori kategoriler eklendi")
        
        // Favori türler ekle
        UserProfile.updateFavoriteTypes(userId, listOf(TvType.Movie, TvType.TvSeries))
        Log.d(TAG, "Favori türler eklendi")
        
        // Örnek içerik ekle
        val sampleContent = ContentItem(
            id = "test_content",
            title = "Test Film",
            normalizedTitle = "test film",
            type = TvType.Movie,
            year = 2023,
            tags = listOf("Dram", "Aksiyon")
        )
        
        // İzleme geçmişine ekle
        UserProfile.addToWatchHistory(userId, sampleContent, "DiziBox")
        Log.d(TAG, "İzleme geçmişine eklendi")
        
        // İstatistikleri al
        val stats = UserProfile.getUserStats(userId)
        Log.d(TAG, "Kullanıcı istatistikleri: ${stats?.totalWatched} izlenen içerik")
        
        // Kişiselleştirilmiş öneriler
        val allContent = listOf(sampleContent)
        val recommendations = UserProfile.getPersonalizedRecommendations(userId, allContent, 5)
        Log.d(TAG, "Kişiselleştirilmiş öneriler: ${recommendations.size} içerik")
    }
    
    /**
     * Bildirim sistemi testi
     */
    fun notificationSystemTest() {
        Log.d(TAG, "=== Bildirim Sistemi Testi ===")
        
        val userId = "test_user_123"
        
        // Bildirim ayarları oluştur
        val settings = NotificationSystem.createNotificationSettings(userId)
        Log.d(TAG, "Bildirim ayarları oluşturuldu")
        
        // Örnek içerik
        val sampleContent = ContentItem(
            id = "test_content",
            title = "Test Film",
            normalizedTitle = "test film",
            type = TvType.Movie,
            year = 2023
        )
        
        // Yeni içerik bildirimi
        NotificationSystem.notifyNewContent(sampleContent, listOf(userId))
        Log.d(TAG, "Yeni içerik bildirimi gönderildi")
        
        // Öneri bildirimi
        NotificationSystem.notifyRecommendation(userId, sampleContent)
        Log.d(TAG, "Öneri bildirimi gönderildi")
        
        // Sistem güncellemesi
        NotificationSystem.notifySystemUpdate("Sistem Güncellemesi", "Yeni özellikler eklendi")
        Log.d(TAG, "Sistem güncellemesi bildirimi gönderildi")
        
        // Kullanıcı bildirimlerini al
        val notifications = NotificationSystem.getUserNotifications(userId, 10)
        Log.d(TAG, "Kullanıcı bildirimleri: ${notifications.size} bildirim")
        
        // Okunmamış bildirimler
        val unreadNotifications = NotificationSystem.getUnreadNotifications(userId)
        Log.d(TAG, "Okunmamış bildirimler: ${unreadNotifications.size} bildirim")
        
        // Bildirimi okundu olarak işaretle
        if (unreadNotifications.isNotEmpty()) {
            NotificationSystem.markAsRead(unreadNotifications.first().id)
            Log.d(TAG, "Bildirim okundu olarak işaretlendi")
        }
    }
    
    /**
     * Tüm örnekleri çalıştır
     */
    fun runAllExamples() {
        normalizationExamples()
        similarityExamples()
        basicUsageExample()
        jsonApiExample()
        performanceTest()
        recommendationTest()
        typeBasedSearchTest()
        mainPageCategoriesTest()
        advancedSearchTest()
        userProfileTest()
        notificationSystemTest()
    }
} 