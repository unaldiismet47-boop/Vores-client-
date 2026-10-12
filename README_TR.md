# VORES Client v2 (tek jar)

## Derleme (GitHub, ücretsiz)
1. github.com -> yeni repo oluştur.
2. Bu klasörün İÇİNDEKİ her şeyi repoya yükle (src, build.gradle, ... ve .github).
   .github klasörü yüklenmediyse: "Create new file" -> adı: `.github/workflows/build.yml`
   -> içine `build-workflow.yml` dosyasındaki yazıyı yapıştır.
3. Actions -> build -> Run workflow.
4. Bitince Artifacts -> vores-client-jar indir (zip içinden jar çıkar).

## Kurulum
- ESKİ vores-client-1_0_0.jar'ı mods klasöründen SİL (yeni jar onu içinde taşıyor).
- Yeni jar + Fabric API -> .minecraft/mods

## Kullanım
- G: VORES menüsü (sol tık aç/kapat, sağ tık ayarlar, slider'ları sürükle)
- C: Zoom, J: Kozmetikler
- ESC menüsü: sol üstte Kozmetikler / Modlar / Skin (tek oyunculu + her sunucu)
