package com.keyiflerolsun

import java.util.*

/**
 * İçerik başlıklarını normalize edip eşleştirme yapan sınıf
 */
object ContentNormalizer {
    
    // Türkçe stop words listesi
    private val stopWords = setOf(
        "ve", "veya", "ile", "için", "bu", "şu", "o", "bir", "iki", "üç", "dört", "beş",
        "altı", "yedi", "sekiz", "dokuz", "on", "yıl", "yılı", "sezon", "bölüm", "film",
        "dizi", "seri", "türkçe", "altyazı", "dublaj", "hd", "full", "izle", "izlesene",
        "the", "a", "an", "and", "or", "for", "of", "in", "on", "at", "to", "with",
        "season", "episode", "movie", "series", "tv", "show", "hd", "full", "watch"
    )
    
    /**
     * Başlığı normalize eder
     */
    fun normalizeTitle(title: String): String {
        return title
            .lowercase(Locale("tr"))
            .replace(Regex("[^a-zçğıöşü0-9\\s]"), " ") // Özel karakterleri kaldır
            .replace(Regex("\\s+"), " ") // Çoklu boşlukları tek boşluğa çevir
            .trim()
            .split(" ")
            .filter { it.isNotBlank() && it !in stopWords && it.length > 1 }
            .joinToString(" ")
    }
    
    /**
     * İki başlığın benzerlik oranını hesaplar (0.0 - 1.0)
     */
    fun calculateSimilarity(title1: String, title2: String): Double {
        val normalized1 = normalizeTitle(title1)
        val normalized2 = normalizeTitle(title2)
        
        if (normalized1 == normalized2) return 1.0
        
        val words1 = normalized1.split(" ").toSet()
        val words2 = normalized2.split(" ").toSet()
        
        if (words1.isEmpty() || words2.isEmpty()) return 0.0
        
        val intersection = words1.intersect(words2).size
        val union = words1.union(words2).size
        
        return intersection.toDouble() / union.toDouble()
    }
    
    /**
     * İki başlığın eşleşip eşleşmediğini kontrol eder
     */
    fun isMatch(title1: String, title2: String, threshold: Double = 0.7): Boolean {
        return calculateSimilarity(title1, title2) >= threshold
    }
    
    /**
     * Benzersiz ID oluşturur
     */
    fun generateId(title: String): String {
        return normalizeTitle(title)
            .replace(" ", "_")
            .replace(Regex("[^a-z0-9_]"), "")
    }
    
    /**
     * Yıl bilgisini başlıktan çıkarır
     */
    fun extractYear(title: String): Int? {
        val yearRegex = Regex("""(19|20)\d{2}""")
        return yearRegex.find(title)?.value?.toIntOrNull()
    }
    
    /**
     * Başlıktan yıl bilgisini temizler
     */
    fun cleanTitleFromYear(title: String): String {
        return title.replace(Regex("""\s*\(?(19|20)\d{2}\)?\s*"""), " ").trim()
    }
    
    /**
     * Kalite bilgisini başlıktan çıkarır
     */
    fun extractQuality(title: String): String? {
        val qualityRegex = Regex("""(4K|2160p|1080p|720p|480p|HD|FHD|UHD)""", RegexOption.IGNORE_CASE)
        return qualityRegex.find(title)?.value?.uppercase()
    }
    
    /**
     * Başlıktan kalite bilgisini temizler
     */
    fun cleanTitleFromQuality(title: String): String {
        return title.replace(Regex("""\s*(4K|2160p|1080p|720p|480p|HD|FHD|UHD)\s*""", RegexOption.IGNORE_CASE), " ").trim()
    }
} 