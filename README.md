# Kekik Reco - Cloudstream Extensions

Bu repository, Cloudstream uygulaması için Türkçe içerik sağlayan eklentileri içerir.

## 📦 Otomatik Derleme

Bu proje GitHub Actions ile otomatik olarak derlenir:

- **Push/Pull Request:** Her değişiklikte otomatik derleme
- **Release:** Yeni sürüm yayınlandığında otomatik paket oluşturma
- **Lint:** Kod kalitesi kontrolü

## 🚀 Hızlı Başlangıç

### Otomatik Derleme Sonuçları
- **Actions sekmesinde** derleme durumunu takip edebilirsiniz
- **Artifacts** bölümünden derlenen dosyaları indirebilirsiniz
- **Releases** sayfasından sürüm dosyalarını bulabilirsiniz

### Manuel Derleme
```bash
# Gereksinimler
- Java 21
- Android SDK
- Gradle

# Derleme
./gradlew clean build

# Eklenti paketi oluşturma
mkdir -p cloudstream-extensions
find . -name "*.aar" | grep "outputs/aar" | grep "release" | xargs -I {} cp {} cloudstream-extensions/
cd cloudstream-extensions && zip -r ../kekik-reco-extensions.zip .
```

## 📱 Cloudstream'e Yükleme

### Yöntem 1: ZIP Dosyası (Önerilen)
1. **Releases** sayfasından `kekik-reco-extensions.zip` dosyasını indirin
2. Cloudstream'i açın
3. **Ayarlar > Eklentiler > Eklenti Yükle**
4. ZIP dosyasını seçin

### Yöntem 2: Tek Tek AAR
1. **Artifacts** bölümünden `cloudstream-extensions` klasörünü indirin
2. Her `.aar` dosyasını ayrı ayrı yükleyin

## 📋 İçerik

### Film Eklentileri
- Filmİzlesene
- FilmKovasi
- HDFilmCehennemi
- HDFilmCehennemi2
- KultFilmler
- Ve daha fazlası...

### Dizi Eklentileri
- DiziPal
- DiziBox
- DiziFun
- DiziGom
- Ve daha fazlası...

### Anime Eklentileri
- AnimeciX
- AnimeIzlesene
- Animeler
- TurkAnime
- Ve daha fazlası...

### Diğer Eklentiler
- NetflixMirror
- YouTube
- PornHub
- Ve 80+ daha fazla eklenti

## 🔧 Geliştirme

### Yeni Eklenti Ekleme
1. Yeni modül klasörü oluşturun
2. `settings.gradle.kts` dosyasına ekleyin
3. Kod yazın ve test edin
4. Pull Request gönderin

### Kod Standartları
- Kotlin kullanın
- Cloudstream API'sine uygun yazın
- Lint kurallarına uyun
- Test yazın

## 📄 Lisans

Bu proje açık kaynak kodludur.

## 🤝 Katkıda Bulunma

1. Fork yapın
2. Feature branch oluşturun (`git checkout -b feature/amazing-feature`)
3. Commit yapın (`git commit -m 'Add amazing feature'`)
4. Push yapın (`git push origin feature/amazing-feature`)
5. Pull Request açın

## 📞 İletişim

- **GitHub Issues:** Hata bildirimi ve öneriler için
- **Discussions:** Genel tartışmalar için

---

⭐ Bu projeyi beğendiyseniz yıldız vermeyi unutmayın!
