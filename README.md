# TermuxIwx 📦

> Package manager & toolbox UI untuk Termux — cari, install, hapus package dengan satu tap dari antarmuka Material Design 3.

[![Build Status](https://github.com/Kztutorial99/TermuxIwx/actions/workflows/build-release.yml/badge.svg)](https://github.com/Kztutorial99/TermuxIwx/actions/workflows/build-release.yml)
[![Latest Release](https://img.shields.io/github/v/release/Kztutorial99/TermuxIwx)](https://github.com/Kztutorial99/TermuxIwx/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/Kztutorial99/TermuxIwx/total)](https://github.com/Kztutorial99/TermuxIwx/releases)
[![Min SDK](https://img.shields.io/badge/minSdk-26%20(Android%207)-brightgreen)](https://developer.android.com/about/versions/nougat)

---

## 🌐 Dashboard

Pantau status build, release, dan traffic langsung dari browser:

**[iwxtermux.vercel.app](https://iwxtermux.vercel.app)**

- 🔨 Status GitHub Actions build real-time
- 📦 Daftar semua rilis + download count
- ⚡ Trigger build baru langsung dari web
- 📱 Dioptimasi untuk mobile

---

## ✨ Fitur

| Fitur | Deskripsi |
|---|---|
| 🔍 **Search Package** | `apt search` real-time dengan debounce 500ms |
| 📦 **Installed List** | Daftar semua package terinstall via `dpkg -l` |
| ⚡ **1-Click Install** | Install package langsung dari UI |
| 🗑 **Remove / Purge** | Hapus package + bersihkan config |
| 🖥 **Console** | Jalankan custom command + riwayat perintah |
| 📋 **Package Detail** | Info lengkap: versi, status, deskripsi |
| 🛠 **Script Repo** | 36 tools populer: Dev, Security, Network, Shell, dll. |
| ⚙ **Settings** | Dark/Light mode, font size, shell, Termux path |
| 🔔 **Auto-Update** | Notifikasi & download otomatis saat ada versi baru |
| ⟳ **Update/Upgrade** | Update repo & upgrade semua package |

---

## 📲 Download

**[⬇ Download APK Terbaru](https://github.com/Kztutorial99/TermuxIwx/releases/latest)**

Atau buka **[iwxtermux.vercel.app](https://iwxtermux.vercel.app)** → tab Releases → Download APK.

---

## 🚀 Cara Pakai

### Syarat
1. Install **Termux** dari [F-Droid](https://f-droid.org/packages/com.termux/) *(bukan Play Store)*
2. Buka Termux, jalankan:
   ```bash
   mkdir -p ~/.termux
   echo "allow-external-apps=true" >> ~/.termux/termux.properties
   ```
3. Restart Termux

### Install APK
1. Download APK dari [Releases](https://github.com/Kztutorial99/TermuxIwx/releases/latest)
2. Buka file APK di Android → tap **Install**
3. Aktifkan *Sumber tidak dikenal* jika diminta

### Auto-Update
App secara otomatis cek versi terbaru sekali per hari. Kalau ada update, muncul dialog untuk download & install langsung dari dalam app. Bisa juga cek manual via **⋮ → 🔄 Cek Update**.

---

## 🔨 Build Sendiri

### Via GitHub Actions (Otomatis)
Push ke branch `main` → APK otomatis ter-build dan di-release ke GitHub Releases.

### Via Android Studio / Gradle
```bash
git clone https://github.com/Kztutorial99/TermuxIwx
cd TermuxIwx
./gradlew assembleDebug
# APK → app/build/outputs/apk/debug/app-debug.apk
```

---

## 🛠 Teknologi

| Komponen | Detail |
|---|---|
| Language | Java 8 |
| Build | Gradle 8.2.2 |
| Min SDK | 26 (Android 7.0 Nougat) |
| Target SDK | 34 (Android 14) |
| UI | Material Design 3, ViewBinding, RecyclerView |
| API | Termux `RUN_COMMAND` Intent |
| CI/CD | GitHub Actions → auto-release APK |
| Dashboard | Next.js 14, Tailwind CSS, Vercel |

---

## 📁 Struktur Project

```
app/src/main/java/com/kztutorial/termuxiwx/
├── models/          # Package, ScriptItem
├── ui/              # MainActivity, ConsoleActivity, PackageDetailActivity,
│                    # ScriptRepoActivity, SettingsActivity
└── utils/           # TermuxConnector, AppSettings, UpdateChecker, APKDownloader

dashboard/           # Web dashboard (Next.js) → iwxtermux.vercel.app
.github/workflows/   # GitHub Actions CI/CD
```

---

## 👨‍💻 Developer

**Kztutorial99** — [GitHub](https://github.com/Kztutorial99/TermuxIwx)

---

*Lihat [CHANGELOG.md](CHANGELOG.md) untuk riwayat perubahan lengkap.*
