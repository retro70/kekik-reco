# 🎬 Kekik-Reco: Birleşik İçerik Havuzu Sistemi

Kekik-Reco, Cloudstream eklentisi için geliştirilmiş gelişmiş bir içerik birleştirme ve yönetim sistemidir. Birden fazla kaynaktan gelen içerikleri akıllı algoritmalarla birleştirerek kullanıcılara tek bir birleşik içerik havuzu sunar.

## 🚀 Özellikler

### 🔍 Temel Özellikler
- **İçerik Normalizasyonu**: Başlıkları normalize ederek benzer içerikleri eşleştirir
- **Akıllı Eşleştirme**: Levenshtein mesafesi ve benzerlik algoritmaları kullanır
- **Çoklu Kaynak Desteği**: Birden fazla kaynaktan içerik toplar
- **Önbellekleme**: Performans için sonuçları önbelleğe alır
- **JSON API**: Frontend entegrasyonu için JSON endpoint'leri

### 🎯 Arama ve Filtreleme
- **Tür Bazlı Arama**: Film, dizi, anime türlerine göre filtreleme
- **Kategori Bazlı Arama**: Etiketlere göre içerik filtreleme
- **Gelişmiş Arama**: Çoklu kriter filtreleme, sıralama, sayfalama
- **Otomatik Tamamlama**: Arama önerileri ve otomatik tamamlama
- **Arama Geçmişi**: Kullanıcı arama geçmişi ve popüler aramalar

### 📊 Analitik ve İstatistikler
- **İçerik İstatistikleri**: Tür, kategori ve yıl bazında istatistikler
- **Kullanıcı İstatistikleri**: İzleme geçmişi ve kullanım analizi
- **Performans Metrikleri**: Sistem performans ve kullanım istatistikleri

### 🎨 Kişiselleştirme
- **Kullanıcı Profili**: Kişiselleştirilmiş kullanıcı profilleri ve tercihler
- **İzleme Geçmişi**: Kullanıcı izleme geçmişi ve istatistikleri
- **Kişiselleştirilmiş Öneriler**: Kullanıcı tercihlerine dayalı akıllı öneriler
- **Favori Kategoriler**: Kullanıcı favori kategorileri ve türleri

### 🔔 Bildirim Sistemi
- **Yeni İçerik Bildirimleri**: Yeni eklenen içerikler için bildirimler
- **Öneri Bildirimleri**: Kişiselleştirilmiş içerik önerileri
- **Sistem Bildirimleri**: Güncelleme ve bakım bildirimleri
- **Bildirim Ayarları**: Kullanıcı bildirim tercihleri ve sessiz saatler

### 🏠 Ana Sayfa ve Kategoriler
- **Ana Sayfa Kategorileri**: Popüler, yeni, trend içerikler
- **Özel Koleksiyonlar**: Özel kategoriler ve koleksiyonlar
- **Akıllı Öneriler**: İçerik benzerliği ve popülerliğe dayalı öneriler

## 🏗️ Mimari

### Ana Bileşenler

#### 1. ContentPoolManager
- İçerik havuzunu yönetir
- Kaynaklardan içerik toplar
- İçerikleri birleştirir ve önbelleğe alır

#### 2. ContentNormalizer
- Başlık normalizasyonu
- Stop word kaldırma
- Benzerlik hesaplama
- ID oluşturma

#### 3. UnifiedSearchPlugin
- Cloudstream eklenti entegrasyonu
- Arama ve yükleme işlemleri
- İçerik dönüştürme

#### 4. UserProfile
- Kullanıcı profili yönetimi
- İzleme geçmişi
- Kişiselleştirilmiş öneriler

#### 5. NotificationSystem
- Bildirim yönetimi
- Bildirim ayarları
- Bildirim türleri ve öncelikleri

#### 6. AdvancedSearch
- Gelişmiş arama algoritmaları
- Filtreleme ve sıralama
- Otomatik tamamlama

#### 7. JsonApi
- JSON API endpoint'leri
- Frontend entegrasyonu
- Hata yönetimi

## 📁 Dosya Yapısı

```
__Temel/
├── src/main/kotlin/com/keyiflerolsun/
│   ├── ContentItem.kt              # İçerik veri yapısı
│   ├── ContentSource.kt            # Kaynak veri yapısı
│   ├── ContentNormalizer.kt        # İçerik normalizasyonu
│   ├── ContentPoolManager.kt       # İçerik havuzu yönetimi
│   ├── UnifiedSearchPlugin.kt      # Ana Cloudstream eklentisi
│   ├── JsonApi.kt                  # JSON API endpoint'leri
│   ├── ContentRecommendation.kt    # Akıllı öneriler
│   ├── MainPageCategories.kt       # Ana sayfa kategorileri
│   ├── AdvancedSearch.kt           # Gelişmiş arama
│   ├── UserProfile.kt              # Kullanıcı profili
│   ├── NotificationSystem.kt       # Bildirim sistemi
│   └── ContentPoolExample.kt       # Kullanım örnekleri
├── build.gradle.kts                # Gradle yapılandırması
└── README.md                       # Bu dosya
```

## 🚀 Kurulum ve Kullanım

### Gereksinimler
- Android Studio
- Kotlin 1.8+
- Cloudstream 3.x

### Kurulum
1. Projeyi klonlayın
2. Android Studio'da açın
3. Gradle sync yapın
4. Build edin

### Temel Kullanım

```kotlin
// İçerik havuzu yöneticisi oluştur
val poolManager = ContentPoolManager()

// Kaynak ekle
poolManager.addSource("DiziBox", diziBoxPlugin)
poolManager.addSource("JetFilm", jetFilmPlugin)

// Arama yap
val results = poolManager.search("Breaking Bad")

// JSON API kullan
val jsonResults = JsonApi.searchJson("Breaking Bad")
```

## 📡 API Endpoint'leri

### Arama API'leri
- `GET /api/search?q={query}` - Genel arama
- `GET /api/search/type/{type}?q={query}` - Tür bazlı arama
- `GET /api/search/genre/{genre}?q={query}` - Kategori bazlı arama
- `POST /api/search/advanced` - Gelişmiş arama

### İçerik API'leri
- `GET /api/content/{id}` - İçerik detayları
- `GET /api/content/type/{type}` - Tür bazlı içerikler
- `GET /api/content/genre/{genre}` - Kategori bazlı içerikler

### İstatistik API'leri
- `GET /api/stats/overview` - Genel istatistikler
- `GET /api/stats/types` - Tür istatistikleri
- `GET /api/stats/genres` - Kategori istatistikleri
- `GET /api/stats/years` - Yıl istatistikleri

### Kullanıcı API'leri
- `POST /api/user/profile` - Kullanıcı profili oluştur
- `GET /api/user/profile/{userId}` - Kullanıcı profili al
- `PUT /api/user/preferences/{userId}` - Kullanıcı tercihleri güncelle
- `POST /api/user/history/{userId}` - İzleme geçmişine ekle
- `GET /api/user/stats/{userId}` - Kullanıcı istatistikleri
- `GET /api/user/recommendations/{userId}` - Kişiselleştirilmiş öneriler

### Bildirim API'leri
- `GET /api/notifications/{userId}` - Kullanıcı bildirimleri
- `GET /api/notifications/{userId}/unread` - Okunmamış bildirimler
- `PUT /api/notifications/{id}/read` - Bildirimi okundu işaretle
- `PUT /api/notifications/{userId}/read-all` - Tüm bildirimleri okundu işaretle
- `GET /api/notifications/settings/{userId}` - Bildirim ayarları
- `PUT /api/notifications/settings/{userId}` - Bildirim ayarlarını güncelle

### Ana Sayfa API'leri
- `GET /api/main/categories` - Ana sayfa kategorileri
- `GET /api/main/popular` - Popüler içerikler
- `GET /api/main/new` - Yeni içerikler
- `GET /api/main/trending` - Trend içerikler

### Arama API'leri
- `GET /api/search/autocomplete?q={query}` - Otomatik tamamlama
- `GET /api/search/history/{userId}` - Arama geçmişi
- `GET /api/search/popular` - Popüler aramalar
- `GET /api/search/similar?q={query}` - Benzer aramalar

## 🔧 Yapılandırma

### ContentPoolManager Ayarları
```kotlin
val config = ContentPoolConfig(
    maxCacheSize = 1000,
    cacheExpirationHours = 24,
    similarityThreshold = 0.8,
    maxResultsPerSearch = 50
)
```

### Bildirim Ayarları
```kotlin
val settings = NotificationSettings(
    enabled = true,
    newContent = true,
    recommendations = true,
    quietHours = false,
    quietHoursStart = 22,
    quietHoursEnd = 8
)
```

## 📊 Performans

### Önbellekleme
- İçerik sonuçları 24 saat önbelleğe alınır
- Arama sonuçları 1 saat önbelleğe alınır
- Kullanıcı profilleri 7 gün önbelleğe alınır

### Optimizasyon
- Paralel arama işlemleri
- Lazy loading
- Memory-efficient data structures
- Background processing

## 🧪 Test

### Test Çalıştırma
```kotlin
// Tüm testleri çalıştır
ContentPoolExample.runAllExamples()

// Belirli testleri çalıştır
ContentPoolExample.normalizationExamples()
ContentPoolExample.userProfileTest()
ContentPoolExample.notificationSystemTest()
```

### Test Kapsamı
- İçerik normalizasyonu
- Benzerlik hesaplama
- Arama performansı
- Kullanıcı profili
- Bildirim sistemi
- JSON API

## 🤝 Katkıda Bulunma

1. Fork yapın
2. Feature branch oluşturun (`git checkout -b feature/amazing-feature`)
3. Commit yapın (`git commit -m 'Add amazing feature'`)
4. Push yapın (`git push origin feature/amazing-feature`)
5. Pull Request oluşturun

## 📝 Lisans

Bu proje MIT lisansı altında lisanslanmıştır. Detaylar için `LICENSE` dosyasına bakın.

## 🆘 Destek

- **Issues**: GitHub Issues kullanın
- **Discussions**: GitHub Discussions kullanın
- **Wiki**: Proje wiki'sini inceleyin

## 🔄 Güncellemeler

### v2.0.0 (Güncel)
- Kullanıcı profili sistemi eklendi
- Bildirim sistemi eklendi
- Gelişmiş arama özellikleri
- Kişiselleştirilmiş öneriler
- Ana sayfa kategorileri
- JSON API genişletildi

### v1.0.0
- Temel içerik birleştirme
- Basit arama
- JSON API
- Önbellekleme

## 📞 İletişim

- **Proje Sahibi**: [@keyiflerolsun](https://github.com/keyiflerolsun)
- **Proje Linki**: [https://github.com/keyiflerolsun/kekik-reco](https://github.com/keyiflerolsun/kekik-reco)

---

⭐ Bu projeyi beğendiyseniz yıldız vermeyi unutmayın! 