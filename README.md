# TermuxIwx 📦

Package manager & toolbox UI untuk Termux — cari, install, hapus package dengan satu tap.

## Fitur
- 🔍 **Search Package** — apt search real-time
- ⚡ **1-Click Install** — install package langsung dari UI
- 🗑 **Remove Package** — hapus package dengan konfirmasi
- 📦 **Installed List** — lihat semua package terinstall
- 🖥 **Console** — jalankan custom command
- ⟳ **Update/Upgrade** — update repo & upgrade semua package
- 📋 **Package Info** — detail info setiap package

## Cara Pakai

### Syarat
1. Install **Termux** dari [F-Droid](https://f-droid.org/packages/com.termux/)
2. Buka Termux, jalankan:
   ```bash
   mkdir -p ~/.termux
   echo "allow-external-apps=true" >> ~/.termux/termux.properties
   ```
3. Restart Termux

### Install APK
Download APK dari [Releases](../../releases) atau [Actions](../../actions)

## Build Sendiri

### Via GitHub Actions (Otomatis)
Push ke branch `main` → APK otomatis ter-build dan tersedia di tab **Actions > Artifacts**

### Via Android Studio
```bash
git clone https://github.com/Kztutorial99/TermuxIwx
cd TermuxIwx
./gradlew assembleDebug
```
APK ada di `app/build/outputs/apk/debug/`

## Teknologi
- Android SDK 34 (Android 14)
- Min SDK 24 (Android 7.0)
- Java 8
- Material Design 3
- Termux RUN_COMMAND Intent API

## Developer
Kztutorial99
