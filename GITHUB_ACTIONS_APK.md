# GitHub Actions ile APK oluşturma

1. GitHub'da yeni bir repository oluştur.
2. Bu ZIP'i aç ve içindeki tüm dosyaları repository'ye yükle.
3. `.github/workflows/build-apk.yml` dosyasının repository'de bulunduğunu kontrol et.
4. GitHub'da **Actions** sekmesine gir.
5. **Build Android APK** workflow'unu seç.
6. **Run workflow** düğmesine bas.
7. İşlem tamamlanınca workflow çalışmasına gir.
8. Sayfanın altındaki **Artifacts** bölümünden `tasit-gorev-kayit-apk` dosyasını indir.
9. ZIP'i aç; içindeki APK'yı Android telefona yükleyebilirsin.

Not:
- Bu workflow debug APK üretir.
- Android Studio gerekmez.
- GitHub Actions'ın Android SDK/JDK ortamını kullanır.
- Eğer proje farklı bir Java/Android SDK sürümü gerektiriyorsa Actions çıktısındaki hataya göre sürümü ayarlayabiliriz.
