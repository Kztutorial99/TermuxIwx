# Changelog — TermuxIwx

Semua perubahan signifikan pada project ini didokumentasikan di file ini.
Format: `[Versi] — Tanggal — Deskripsi`

---

## [1.7.0] — 2026-06-24 — Auto-Update + Web Dashboard + CI/CD Fix

### Ditambahkan — Auto-Update System
- **`UpdateChecker.java`** — cek GitHub Releases API sekali per hari (throttle 24j), bandingkan versionName secara semantik (major.minor.patch), jalankan di background thread
- **`APKDownloader.java`** — download APK baru via Android `DownloadManager`, install otomatis via `ACTION_INSTALL_PACKAGE` saat download selesai
- **Menu "🔄 Cek Update"** — item di action bar MainActivity untuk trigger cek update manual
- **Dialog update Material** — tampil saat ada versi baru: tombol **Download**, **Lihat di GitHub**, **Nanti**
- **Permission `REQUEST_INSTALL_PACKAGES`** — ditambahkan ke `AndroidManifest.xml`
- **`FileProvider`** — dikonfigurasi di `AndroidManifest.xml` + `file_provider_paths.xml` untuk share file APK
- **`scheduleUpdateCheck()`** — dipanggil saat `MainActivity.onStart()`, delay 3 detik agar tidak blok UI launch

### Ditambahkan — Web Dashboard (`iwxtermux.vercel.app`)
- **Dashboard real-time** — monitor status build, release, dan traffic TermuxIwx dari browser
- **Tab Dashboard** — status build terkini, stats (stars, forks, total downloads, versi terbaru), 5 build terakhir
- **Tab Builds** — riwayat lengkap semua GitHub Actions run dengan status badge berwarna
- **Tab Releases** — daftar semua APK release + download count + progress bar proporsi download + tombol download langsung
- **Tab Trigger** — picu build baru via `workflow_dispatch` GitHub API dengan admin key + catatan rilis kustom
- **Auto-refresh 30 detik** — data diperbarui otomatis tanpa reload manual
- **Mobile-first** — bottom navigation, dark theme, responsive cards; dioptimasi untuk layar HP
- **API Routes (Next.js)** — `/api/builds`, `/api/releases`, `/api/trigger` — proxy ke GitHub API server-side (token aman, tidak expose ke client)
- **Deploy ke Vercel** — project `iwxtermux`, URL: [iwxtermux.vercel.app](https://iwxtermux.vercel.app)

### Diperbaiki — GitHub Actions CI/CD
- **Step "Create GitHub Release" gagal** — fix: tambah `permissions: contents: write` di job level; buat git tag secara eksplisit via `git tag -f` + `git push --force` sebelum `softprops/action-gh-release@v2` dipanggil
- **`workflow_dispatch` input** — tambah input `release_notes` agar bisa isi catatan rilis dari dashboard web atau GitHub UI langsung
- **`actions/checkout@v4`** — tambah `persist-credentials: true` agar step push tag berhasil
- **Release body otomatis** — body release kini berisi ringkasan fitur, cara install, dan syarat Termux

### File Baru
- `app/src/main/java/com/kztutorial/termuxiwx/utils/UpdateChecker.java`
- `app/src/main/java/com/kztutorial/termuxiwx/utils/APKDownloader.java`
- `app/src/main/res/xml/file_provider_paths.xml`
- `dashboard/` — seluruh direktori Next.js web dashboard
- `.github/workflows/build-release.yml` — workflow CI/CD yang diperbarui

### File Diubah
- `AndroidManifest.xml` — tambah `FileProvider`, `REQUEST_INSTALL_PACKAGES`, `UpdateChecker` dipanggil
- `MainActivity.java` — `scheduleUpdateCheck()`, `showUpdateDialog()`, `startAPKDownload()`, menu "Cek Update"
- `app/src/main/res/menu/main_menu.xml` — tambah item "🔄 Cek Update"

---

## [1.6.0] — 2026-06-24 — Modernisasi UX/UI & Audit Menyeluruh

### Diperbaiki — Bug Kritis (P1)
- **Filter chip Database/Editor/Media di ScriptRepo tidak ada** — 9 tools (mariadb, postgresql, redis, vim, neovim, ffmpeg, imagemagick) tidak bisa difilter sama sekali. Fix: tambah chip XML + binding + logic di `setupCategoryChips()` dan `getCurrentCategory()`
- **Silent failure saat hapus package gagal** — `handleResult(ACTION_REMOVE)` tidak punya branch `else` saat exitCode != 0. Fix: tampilkan Snackbar error
- **Tema tidak apply saat app dibuka ulang** — `recreate()` di SettingsActivity hanya berlaku untuk sesi itu. Fix: buat `TermuxIwxApp extends Application` yang panggil `AppCompatDelegate.setDefaultNightMode()` saat startup

### Ditambahkan — Fitur Baru (P1/P2)
- **`apt purge`** di PackageDetailActivity — tombol "Hapus + Bersihkan Config (purge)" muncul saat package terinstall
- **`TermuxConnector.aptPurge()`** — method baru untuk jalankan `apt purge -y <package>`
- **`TermuxIwxApp.java`** — Application class baru, atur tema global lewat `AppCompatDelegate` sebelum activity manapun terbuka

### Modernisasi UX (P2)
- **`DiffUtil` di PackageAdapter & ScriptAdapter** — animasi add/remove/move yang halus, tidak re-render seluruh list
- **Deskripsi package tampil di list** — `item_package.xml` kini punya `pkg_desc` TextView
- **Search debounce 500ms di MainActivity** — pencarian hanya jalan setelah user berhenti mengetik
- **Status card Termux OK auto-dismiss** — kartu hijau "Termux terdeteksi ✓" otomatis hilang setelah 3 detik
- **About dialog versi benar** — diperbaiki ke "v1.5.0" + tambah warning Termux Play Store

### Upgrade Komponen (P2/P3)
- **Toast → Snackbar** di semua Activity — sesuai Material Design 3 guideline
- **`onBackPressed()` deprecated → `OnBackPressedCallback`** di ConsoleActivity, PackageDetailActivity, ScriptRepoActivity
- **`item_package.xml` & `activity_package_detail.xml`** — upgrade ke `MaterialCardView` dengan `strokeColor` border
- **`pkg_version` di list** — warna diubah ke `colorPrimary` (hijau) + `fontFamily=monospace`

### File Baru
- `TermuxIwxApp.java`

---

## [1.5.0] — 2026-06-24 — Bug Fix Besar (Audit P1–P3)

### Diperbaiki — Bug Kritis (P1)
- **Tab Installed selalu kosong** — `handleResult()` pakai `parseFromAptSearch()` untuk output `dpkg -l` yang format-nya beda. Fix: tambah `Package.parseFromDpkg()` yang parse format `ii name version arch desc`
- **Settings "Termux Path" tidak efek** — `TermuxConnector` hardcode semua path binary. Fix: semua method kini pakai `AppSettings.getTermuxPath()` secara dinamis
- **Ganti tema tidak apply** — bug logika di `SettingsActivity.saveSettings()`. Fix: simpan nilai tema lama sebelum `setTheme()`, bandingkan setelahnya
- **Storage Info di-parse sebagai package** — Fix: tambah state `CMD_STORAGE` yang tampilkan hasil di AlertDialog

### Diperbaiki — Dead Code & Setting Tidak Aktif (P2)
- **`isFilterStderr` & `isShowExitCode` tidak dipakai** — kini dicek sebelum dijalankan
- **Permission `RECEIVE_BOOT_COMPLETED` tidak terpakai** — dihapus dari `AndroidManifest.xml`
- **`CMD_UPGRADE` tidak punya state** — Fix: tambah `CMD_UPGRADE = 4`

### Ditambahkan — Fitur Baru (P3)
- **Command History di Console** — tombol ▲/▼ untuk scroll riwayat perintah (max 50 entry)
- **Warna kategori lengkap di ScriptAdapter** — Database (biru), Editor (ungu), Media (merah muda), Termux (hijau)
- **Fix typo install vim** — perintah `lvim` diperbaiki ke `vim`

### Refactor
- **`ScriptItem`** — field `public` → `private` dengan getters proper
- **`AppSettings`** — tambah konstanta font size
- **`Package.parseFromAptSearch()`** — diperbaiki untuk juga ekstrak deskripsi

---

## [1.4.0] — 2026-06-24 — Settings Screen

### Ditambahkan
- **SettingsActivity** — Termux Path, Default Shell, Ukuran Font Console, Tampilkan Exit Code, Filter APT Warnings, Tema Aplikasi (Dark/Light)
- **AppSettings.java** — helper SharedPreferences terpusat
- Tombol ⚙ Settings di action bar MainActivity

### File Baru
- `SettingsActivity.java`, `activity_settings.xml`, `AppSettings.java`

---

## [1.3.0] — 2026-06-24 — Script & Tools Repository

### Ditambahkan
- **ScriptRepoActivity** — repository 36 tools populer Termux: Dev, Security, Network, Tool, Shell, Media, Editor, Database, Termux
- Search realtime + filter chip per kategori
- Dialog install: pilih ⚡ Install langsung atau 🖥 Buka Console
- Tab "Scripts" di MainActivity kini membuka ScriptRepoActivity

### File Baru
- `ScriptRepoActivity.java`, `ScriptItem.java`, `ScriptAdapter.java`, `activity_script_repo.xml`, `item_script.xml`

---

## [1.2.0] — 2026-06-24 — Console Fixes & Quick Commands

### Diperbaiki
- **Bug Double Prompt** — prompt hanya ditambahkan satu kali di `runCommand()`
- **Filter Stderr Noise** — warning APT difilter otomatis
- **Timer Elapsed** — counter `⏳ Menjalankan... 0s, 1s, 2s...` via `Handler.postDelayed`
- **Progress Bar** — tampil selama command berjalan

### Ditambahkan
- **Command Not Found Detection** — dialog tawaran install otomatis (mapping 30+ perintah → package)
- **Quick Command Chips** — `ls`, `pkg update`, `pwd`, `df -h`, `pkg list-installed`, `uname -a`

---

## [1.1.1] — 2026-06-23 — Build Fix: BroadcastReceiver Context

### Diperbaiki
- **Compile error** di `ScriptRepoActivity.java`: `Toast.makeText(this, ...)` di dalam `BroadcastReceiver.onReceive()`. Fix: ganti `this` → `ScriptRepoActivity.this`

---

## [1.1.0] — 2026-06-23 — Build Fix: Adaptive Icon & minSdk

### Diperbaiki
- **Build error** `adaptive-icon requires sdk >= 26`: ikon adaptive dipindah ke `mipmap-anydpi-v26/`
- `minSdk` dinaikkan dari 21 ke **26**; `targetSdk` dan `compileSdk` diset ke **34**

### GitHub Actions
- Workflow CI/CD dikonfigurasi: auto-create GitHub Release setiap push ke `main`

---

## [1.0.0] — 2026-06-23 — Initial Release

### Fitur Awal
- **Package Search** — cari package via `apt-cache search`
- **Installed Packages** — daftar package via `dpkg --list`
- **PackageDetailActivity** — detail, Install / Remove 1-click
- **ConsoleActivity** — terminal sederhana
- **MainActivity** — 4 tab: Search, Installed, Scripts, Console
- **TermuxConnector** — wrapper Intent `RUN_COMMAND`
- **GitHub Actions** — build otomatis APK debug

### Spesifikasi Teknis
- Package: `com.kztutorial.termuxiwx`
- Language: Java (Android) | minSdk: 26 | targetSdk: 34
- Requires: Termux dari F-Droid + `allow-external-apps=true`

---

*Changelog diperbarui setiap ada perubahan signifikan pada project.*
