# Changelog — TermuxIwx

Semua perubahan signifikan pada project ini didokumentasikan di file ini.
Format: `[Versi] — Tanggal — Deskripsi`

---

## [1.5.0] — 2025-06-24 — Bug Fix Besar (Audit P1–P3)

### Diperbaiki — Bug Kritis (P1)
- **Tab Installed selalu kosong** — `handleResult()` sebelumnya pakai `parseFromAptSearch()` untuk output `dpkg -l` yang format-nya beda total. Fix: tambah method `Package.parseFromDpkg()` yang parse format `ii name version arch desc` dengan benar
- **Settings "Termux Path" tidak efek** — `TermuxConnector` sebelumnya hardcode semua path binary. Fix: semua method kini pakai `AppSettings.getTermuxPath()` secara dinamis via helper `getBinPath(context)` dan `getShell(context)`
- **Ganti tema tidak apply** — bug logika di `SettingsActivity.saveSettings()`: `settings.isLightTheme()` dipanggil setelah tema disimpan sehingga kondisi selalu false dan `recreate()` tidak pernah jalan. Fix: simpan nilai tema lama sebelum `setTheme()`, bandingkan setelahnya
- **Storage Info di-parse sebagai package** — menu Storage Info kirim hasil ke `handleResult()` yang mencoba parse `df -h` sebagai daftar package. Fix: tambah state `CMD_STORAGE` yang tampilkan hasil di AlertDialog

### Diperbaiki — Dead Code & Setting Tidak Aktif (P2)
- **`isFilterStderr` tidak dipakai** — `ConsoleActivity` selalu filter stderr tanpa cek preferensi user. Fix: cek `settings.isFilterStderr()` sebelum filter
- **`isShowExitCode` tidak dipakai** — exit code selalu ditampilkan. Fix: cek `settings.isShowExitCode()` sebelum append status akhir
- **Permission `RECEIVE_BOOT_COMPLETED` tidak terpakai** — dihapus dari `AndroidManifest.xml`
- **`CMD_UPGRADE` tidak punya state** — upgrade sebelumnya tidak punya `currentCommand` sendiri sehingga hasil upgrade mencoba di-parse sebagai package. Fix: tambah `CMD_UPGRADE = 4`

### Ditambahkan — Fitur Baru (P3)
- **Command History di Console** — tombol ▲ (naik) dan ▼ (turun) untuk scroll riwayat perintah sebelumnya (max 50 entry). Duplikat berurutan tidak disimpan
- **Warna kategori lengkap di ScriptAdapter** — Database (biru `#58A6FF`), Editor (ungu `#BC8CFF`), Media (merah muda `#FF7B72`), Termux (hijau `success_color`) kini punya warna berbeda — sebelumnya semua fallback ke abu-abu
- **Fix typo install vim** — perintah install vim yang salah (`lvim`) diperbaiki ke `vim`

### Refactor
- **`ScriptItem`** — field diubah dari `public` ke `private` dengan getters proper (`getName()`, `getDescription()`, `getInstallCmd()`, `getCategory()`, `getTestCmd()`)
- **`AppSettings`** — konstanta `FONT_SIZE_SMALL_SP`, `FONT_SIZE_MEDIUM_SP`, `FONT_SIZE_LARGE_SP` ditambahkan agar tidak ada magic number duplikat
- **`Package.parseFromAptSearch()`** — diperbaiki untuk juga ekstrak deskripsi dari output `apt search`
- **`MainActivity` tab Scripts/Console** — setelah launch Activity, tab otomatis kembali ke posisi Search agar state visual tidak out-of-sync saat user kembali

### File Diubah
- `AndroidManifest.xml`
- `Package.java`
- `ScriptItem.java`
- `AppSettings.java`
- `TermuxConnector.java`
- `MainActivity.java`
- `ConsoleActivity.java`
- `SettingsActivity.java`
- `ScriptRepoActivity.java`
- `ScriptAdapter.java`
- `activity_console.xml`
- `colors.xml`

---

## [1.4.0] — 2025-06-24 — Settings Screen

### Ditambahkan
- **SettingsActivity** — halaman pengaturan lengkap dengan:
  - **Termux Path** — input path binary Termux (default: `/data/data/com.termux/files/usr/bin`), tombol reset ke default
  - **Default Shell** — dropdown pilih shell (bash / zsh / fish / sh)
  - **Ukuran Font Console** — pilihan Kecil (11sp) / Sedang (13sp) / Besar (15sp) dengan live preview
  - **Tampilkan Exit Code** — toggle tampilkan `✓ selesai` / `✗ exit` di akhir output console
  - **Filter APT Warnings** — toggle sembunyikan warning APT yang tidak penting
  - **Tema Aplikasi** — toggle Dark / Light mode
- **AppSettings.java** — helper SharedPreferences terpusat untuk semua setting
- Tombol ⚙ Settings ditambahkan di action bar MainActivity
- Tombol "Simpan" & "Reset Semua" di halaman Settings

### File Baru
- `SettingsActivity.java`
- `activity_settings.xml`
- `AppSettings.java`

---

## [1.3.0] — 2025-06-24 — Script & Tools Repository

### Ditambahkan
- **ScriptRepoActivity** — repository 36 tools populer Termux dengan kategori:
  - Dev (python, nodejs, golang, rust, php, ruby, clang, java, pip)
  - Security (nmap, hydra, sqlmap, aircrack-ng, metasploit)
  - Network (openssh, net-tools, iproute2, tmate)
  - Tool (wget, curl, git, tmux, htop, neofetch, ffmpeg, imagemagick)
  - Shell (zsh, fish, oh-my-zsh)
  - Media (ffmpeg, imagemagick)
  - Editor (vim, neovim)
  - Database (mariadb, postgresql, redis)
  - Termux (termux-api, termux-setup-storage)
- Search realtime + filter chip per kategori
- Dialog install: pilih ⚡ Install langsung atau 🖥 Buka Console
- Tab "Scripts" di MainActivity kini membuka ScriptRepoActivity
- **ScriptItem.java** — model data tool/script
- **ScriptAdapter.java** — RecyclerView adapter untuk daftar tools

### File Baru
- `ScriptRepoActivity.java`
- `ScriptItem.java`
- `ScriptAdapter.java`
- `activity_script_repo.xml`
- `item_script.xml`

---

## [1.2.0] — 2025-06-24 — Console Fixes & Quick Commands

### Diperbaiki
- **Bug Double Prompt** — sebelumnya output tampil `$ $ perintah` karena `appendOutput()` menambah trailing `$ ` dan `runCommand()` menambah `$ ` lagi. Fix: prompt hanya ditambahkan satu kali di `runCommand()`
- **Filter Stderr Noise** — warning APT (`WARNING: apt does not have a stable CLI interface`, `Use with caution in scripts`, dll.) kini difilter otomatis dari output console
- **Timer Elapsed** — counter `⏳ Menjalankan... 0s, 1s, 2s...` berjalan tiap detik via `Handler.postDelayed` selama command berjalan
- **Progress Bar** — progress bar indeterminate tampil selama command berjalan, hilang saat selesai
- Input field & tombol Run di-disable saat command sedang berjalan (guard `isRunning`)

### Ditambahkan
- **Command Not Found Detection** — deteksi otomatis kata `not found` / `command not found` / `is not installed` di stdout/stderr, lalu tampilkan dialog:
  - "Install sekarang?" dengan command `pkg install -y <package>` yang sesuai
  - Mapping 30+ perintah → nama package (wget, curl, git, python, node, nmap, ssh, ffmpeg, dll.)
- **Quick Command Chips** — bar chip horizontal di bawah input console:
  - `ls`, `pkg update`, `pwd`, `df -h`, `pkg list-installed`, `uname -a`
  - Tap chip langsung jalankan command tanpa ketik manual

### File Diubah
- `ConsoleActivity.java`
- `activity_console.xml`

---

## [1.1.1] — 2025-06-23 — Build Fix: BroadcastReceiver Context

### Diperbaiki
- **Compile error** di `ScriptRepoActivity.java`: `Toast.makeText(this, ...)` di dalam `BroadcastReceiver.onReceive()` menggunakan konteks receiver bukan Activity. Fix: ganti `this` → `ScriptRepoActivity.this`

---

## [1.1.0] — 2025-06-23 — Build Fix: Adaptive Icon & minSdk

### Diperbaiki
- **Build error** `adaptive-icon requires sdk >= 26`: ikon adaptive dipindah ke `mipmap-anydpi-v26/`, folder mipmap density (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`) diisi fallback layer-list sederhana
- `minSdk` dinaikkan dari 21 ke **26** agar adaptive icon tidak error
- `targetSdk` dan `compileSdk` diset ke **34**

### GitHub Actions
- Workflow `.github/workflows/build.yml` dikonfigurasi ulang: auto-create GitHub Release bertag `latest` setiap push ke `main`
- APK tersedia tanpa login di:
  ```
  https://github.com/Kztutorial99/TermuxIwx/releases/download/latest/TermuxIwx-debug.apk
  ```

---

## [1.0.0] — 2025-06-23 — Initial Release

### Fitur Awal
- **Package Search** — cari package Termux via `apt-cache search`, tampil di RecyclerView
- **Installed Packages** — daftar semua package yang sudah terinstall via `dpkg --list`
- **PackageDetailActivity** — detail package: versi, status install, tombol Install / Remove 1-click
- **ConsoleActivity** — terminal sederhana, input perintah bebas, output ditampilkan di layar
- **MainActivity** — 4 tab: Search, Installed, Scripts, Console
- **TermuxConnector** — wrapper Intent `RUN_COMMAND` untuk komunikasi ke Termux
- **GitHub Actions** — build otomatis APK debug via workflow CI/CD

### Spesifikasi Teknis
- Package: `com.kztutorial.termuxiwx`
- Language: Java (Android)
- minSdk: 26 | targetSdk: 34 | compileSdk: 34
- Requires: Termux dari F-Droid + `allow-external-apps=true`
- Permission: `com.termux.permission.RUN_COMMAND`

---

*Changelog ini dibuat otomatis dan diperbarui setiap ada perubahan signifikan.*
