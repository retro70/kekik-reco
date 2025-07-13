# Kekik Reco - Benzersiz İçerik Havuzu Sistemi

Bu proje, Cloudstream eklentisi için geliştirilmiş benzersiz içerik havuzu sistemidir. Birden fazla kaynaktan gelen aynı içerikleri birleştirerek kullanıcıya tek bir arayüz sunar.

## 🎯 Özellikler

- **Benzersiz İçerik Havuzu**: Aynı içerik birden fazla kaynakta varsa tek bir girdi altında toplanır
- **Akıllı Eşleştirme**: Başlık normalizasyonu ve benzerlik algoritması ile doğru eşleştirme
- **Çoklu Kaynak Desteği**: 80+ farklı kaynak tek sistemde
- **Cache Sistemi**: Performans için akıllı önbellekleme
- **JSON API**: Frontend entegrasyonu için REST API
- **Paralel Arama**: Tüm kaynaklardan eş zamanlı arama
- **Tür Bazlı Filtreleme**: Film, dizi, anime ayrımı
- **Kategori Filtreleme**: Aksiyon, dram, komedi vb. türlere göre filtreleme
- **İstatistik Dashboard**: Tür, kategori ve yıl bazlı istatistikler
- **Akıllı Öneriler**: İçerik bazlı ve kullanıcı geçmişine göre öneriler

## 🏗️ Mimari

### Ana Bileşenler

1. **ContentItem**: Benzersiz içerik veri sınıfı
2. **ContentSource**: Kaynak bilgilerini tutan sınıf
3. **ContentNormalizer**: Başlık normalizasyonu ve eşleştirme
4. **ContentPoolManager**: Ana yönetim sınıfı
5. **UnifiedSearchPlugin**: Cloudstream entegrasyonu
6. **JsonApi**: Frontend API endpoint'leri

### Veri Akışı

```
Kullanıcı Arama → UnifiedSearchPlugin → ContentPoolManager → 
Tüm Kaynaklardan Paralel Arama → ContentNormalizer → 
Benzersiz İçerik Havuzu → JSON Response
```

## 📁 Dosya Yapısı

```
__Temel/src/main/kotlin/com/keyiflerolsun/
├── ContentItem.kt              # Veri sınıfları
├── ContentNormalizer.kt        # Normalizasyon ve eşleştirme
├── ContentPoolManager.kt       # Ana yönetim sınıfı
├── UnifiedSearchPlugin.kt      # Cloudstream eklentisi
├── UnifiedSearchPluginPlugin.kt # Plugin tanımlama
├── JsonApi.kt                  # JSON API endpoint'leri
└── ContentPoolExample.kt       # Kullanım örnekleri
```

## 🚀 Kullanım

### Temel Kullanım

```kotlin
// 1. İçerik havuzu yöneticisini oluştur
val contentPoolManager = ContentPoolManager()

// 2. Kaynakları ekle
contentPoolManager.addSource("DiziBox", DiziBox())
contentPoolManager.addSource("JetFilmizle", JetFilmizle())

// 3. Arama yap
val results = contentPoolManager.searchAndMerge("Breaking Bad")

// 4. Sonuçları işle
results.forEach { contentItem ->
    println("İçerik: ${contentItem.title}")
    println("Kaynaklar: ${contentItem.sources.map { it.sourceName }}")
}
```

### Normalizasyon Örnekleri

```kotlin
// Başlık normalizasyonu
val normalized = ContentNormalizer.normalizeTitle("Breaking Bad (2008) HD Türkçe Dublaj")
// Sonuç: "breaking bad"

// Benzerlik hesaplama
val similarity = ContentNormalizer.calculateSimilarity("Breaking Bad", "Breaking Bad HD")
// Sonuç: 1.0 (tam eşleşme)

// Eşleştirme kontrolü
val isMatch = ContentNormalizer.isMatch("Breaking Bad", "Breaking Bad 2008")
// Sonuç: true
```

### JSON API Kullanımı

```kotlin
// Temel arama
val searchJson = JsonApi.searchJson("Breaking Bad")

// Tür bazlı arama
val typeSearchJson = JsonApi.searchByTypeJson("Breaking Bad", "tvseries")

// Kategori bazlı arama
val genreSearchJson = JsonApi.searchByGenreJson("Breaking Bad", "Dram")

// İçerik detayları
val detailsJson = JsonApi.getContentDetailsJson("content_id")

// İstatistikler
val statsJson = JsonApi.getStatsJson()
val typeStatsJson = JsonApi.getTypeStatsJson()
val genreStatsJson = JsonApi.getGenreStatsJson()

// Öneriler
val recommendationsJson = JsonApi.getContentRecommendationsJson("content_id")
val popularGenresJson = JsonApi.getPopularGenresJson()
val mixedRecommendationsJson = JsonApi.getMixedRecommendationsJson()
```

## 📊 Veri Yapıları

### ContentItem

```kotlin
data class ContentItem(
    val id: String,                    // Benzersiz ID
    val title: String,                 // Orijinal başlık
    val normalizedTitle: String,       // Normalize edilmiş başlık
    val type: TvType,                  // İçerik türü
    val year: Int? = null,             // Yayın yılı
    val poster: String? = null,        // Poster URL
    val description: String? = null,   // Açıklama
    val rating: Int? = null,           // Puan
    val duration: Int? = null,         // Süre
    val tags: List<String> = emptyList(), // Etiketler
    val sources: List<ContentSource> = emptyList(), // Kaynak listesi
    val lastUpdated: Long = System.currentTimeMillis()
)
```

### ContentSource

```kotlin
data class ContentSource(
    val sourceName: String,            // Kaynak adı
    val sourceUrl: String,             // Kaynak URL'i
    val originalTitle: String,         // Orijinal başlık
    val quality: String? = null,       // Kalite bilgisi
    val language: String? = null,      // Dil bilgisi
    val isAvailable: Boolean = true,   // Erişilebilirlik
    val lastChecked: Long = System.currentTimeMillis()
)
```

## 🔧 Konfigürasyon

### Benzerlik Eşiği

```kotlin
// ContentPoolManager.kt içinde
private const val SIMILARITY_THRESHOLD = 0.7 // 0.0 - 1.0 arası
```

### Cache Süresi

```kotlin
// ContentPoolManager.kt içinde
private const val CACHE_EXPIRY_TIME = 24 * 60 * 60 * 1000L // 24 saat
```

## 📈 Performans

### Optimizasyonlar

- **Paralel Arama**: Tüm kaynaklardan eş zamanlı arama
- **Akıllı Cache**: 24 saatlik önbellekleme
- **Lazy Loading**: Gerektiğinde yükleme
- **Memory Management**: ConcurrentHashMap kullanımı

### Beklenen Performans

- **Arama Süresi**: 2-5 saniye (kaynak sayısına bağlı)
- **Cache Hit Rate**: %80+ (tekrarlanan aramalar için)
- **Memory Usage**: ~50MB (1000 içerik için)

## 🧪 Test

### Örnek Test Çalıştırma

```kotlin
// Tüm örnekleri çalıştır
ContentPoolExample.runAllExamples()

// Tek örnek çalıştır
ContentPoolExample.normalizationExamples()
ContentPoolExample.similarityExamples()
ContentPoolExample.performanceTest()
```

### Test Senaryoları

1. **Normalizasyon Testi**: Farklı başlık formatlarının normalize edilmesi
2. **Eşleştirme Testi**: Benzer başlıkların doğru eşleştirilmesi
3. **Performans Testi**: Çoklu arama performansı
4. **Cache Testi**: Önbellekleme işlevselliği

## 🔌 Entegrasyon

### Cloudstream Entegrasyonu

```kotlin
// Plugin tanımlama
@CloudstreamPlugin
class UnifiedSearchPluginPlugin : Plugin() {
    override fun load() {
        registerMainAPI(UnifiedSearchPlugin())
    }
}
```

### Frontend Entegrasyonu

```javascript
// Arama API'si
fetch('/api/search?q=Breaking Bad')
  .then(response => response.json())
  .then(data => {
    console.log('Sonuçlar:', data.results);
  });

// İçerik detayları
fetch('/api/content/content_id')
  .then(response => response.json())
  .then(data => {
    console.log('İçerik:', data.content);
  });
```

## 🐛 Hata Ayıklama

### Log Seviyeleri

```kotlin
// Debug logları
Log.d("ContentPoolManager", "Arama başlatılıyor: $query")
Log.d("ContentPoolManager", "Birleştirme tamamlandı: ${mergedItems.size} benzersiz içerik")

// Hata logları
Log.e("ContentPoolManager", "Kaynak arama hatası ($sourceName): ${e.message}")
```

### Yaygın Hatalar

1. **Kaynak Bağlantı Hatası**: Kaynak erişilemez durumda
2. **Eşleştirme Hatası**: Benzerlik eşiği çok yüksek/düşük
3. **Cache Hatası**: Önbellek süresi dolmuş
4. **Memory Hatası**: Çok fazla içerik yüklenmiş

## 📝 Lisans

Bu proje MIT lisansı altında lisanslanmıştır.

## 🤝 Katkıda Bulunma

1. Fork yapın
2. Feature branch oluşturun (`git checkout -b feature/amazing-feature`)
3. Commit yapın (`git commit -m 'Add amazing feature'`)
4. Push yapın (`git push origin feature/amazing-feature`)
5. Pull Request oluşturun

## 📞 İletişim

- **Geliştirici**: @keyiflerolsun
- **Proje**: Kekik Akademi
- **GitHub**: https://github.com/retro70/kekik-reco 