package com.keyiflerolsun

import com.lagradost.cloudstream3.TvType

/**
 * Benzersiz içerik öğesi - birden fazla kaynaktan gelen aynı içeriği temsil eder
 */
data class ContentItem(
    val id: String,                    // Benzersiz ID (normalize edilmiş başlık)
    val title: String,                 // Orijinal başlık
    val normalizedTitle: String,       // Normalize edilmiş başlık (eşleştirme için)
    val type: TvType,                  // İçerik türü (Movie, TvSeries, etc.)
    val year: Int? = null,             // Yayın yılı
    val poster: String? = null,        // Poster URL
    val description: String? = null,   // Açıklama
    val rating: Int? = null,           // Puan
    val duration: Int? = null,         // Süre (dakika)
    val tags: List<String> = emptyList(), // Etiketler
    val sources: List<ContentSource> = emptyList(), // Kaynak listesi
    val lastUpdated: Long = System.currentTimeMillis() // Son güncelleme zamanı
)

/**
 * İçerik kaynağı bilgisi
 */
data class ContentSource(
    val sourceName: String,            // Kaynak adı (örn: "DiziBox", "JetFilm")
    val sourceUrl: String,             // Kaynak URL'i
    val originalTitle: String,         // Kaynakta görünen orijinal başlık
    val quality: String? = null,       // Kalite bilgisi
    val language: String? = null,      // Dil bilgisi
    val isAvailable: Boolean = true,   // Kaynak erişilebilir mi?
    val lastChecked: Long = System.currentTimeMillis() // Son kontrol zamanı
)

/**
 * Arama sonucu için basitleştirilmiş veri sınıfı
 */
data class SearchResult(
    val title: String,
    val url: String,
    val type: TvType,
    val poster: String? = null,
    val year: Int? = null,
    val sourceName: String
) 