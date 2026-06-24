package com.kztutorial.termuxiwx.ui;

import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.kztutorial.termuxiwx.BuildConfig;
import com.kztutorial.termuxiwx.R;
import com.kztutorial.termuxiwx.databinding.ActivityMainBinding;
import com.kztutorial.termuxiwx.models.Package;
import com.kztutorial.termuxiwx.ui.adapters.PackageAdapter;
import com.kztutorial.termuxiwx.utils.APKDownloader;
import com.kztutorial.termuxiwx.utils.TermuxConnector;
import com.kztutorial.termuxiwx.utils.UpdateChecker;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String ACTION_RESULT   = "com.kztutorial.termuxiwx.RESULT";
    private static final int    CMD_SEARCH      = 1;
    private static final int    CMD_INSTALLED   = 2;
    private static final int    CMD_UPDATE      = 3;
    private static final int    CMD_UPGRADE     = 4;
    private static final int    CMD_STORAGE     = 5;
    private static final long   SEARCH_DEBOUNCE = 500L;
    private static final long   UPDATE_INTERVAL = 24 * 60 * 60 * 1000L; // 1 day

    private ActivityMainBinding binding;
    private PackageAdapter adapter;
    private int  currentCommand = 0;
    private int  lastRealTab    = 0;
    private boolean suppressTabEvent = false;

    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    private final BroadcastReceiver resultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            Bundle extras = intent.getExtras();
            if (extras == null) return;
            Bundle result   = extras.getBundle("result");
            String stdout   = result != null ? result.getString("stdout", "")  : "";
            String stderr   = result != null ? result.getString("stderr", "")  : "";
            int    exitCode = result != null ? result.getInt("exitCode", -1)   : -1;
            runOnUiThread(() -> handleResult(stdout, stderr, exitCode));
        }
    };

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        checkTermuxStatus();
        setupRecyclerView();
        setupTabs();
        setupSearch();
        setupButtons();
        scheduleUpdateCheck();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(ACTION_RESULT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(resultReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(resultReceiver, filter);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        debounceHandler.removeCallbacks(searchRunnable);
        try { unregisterReceiver(resultReceiver); } catch (Exception ignored) {}
    }

    // ─── Update System ────────────────────────────────────────────────────────

    private void scheduleUpdateCheck() {
        long lastCheck = getSharedPreferences("update_prefs", MODE_PRIVATE)
                .getLong("last_check_ts", 0);
        if (System.currentTimeMillis() - lastCheck < UPDATE_INTERVAL) return;

        // Delay check by 3s so UI loads first
        new Handler(Looper.getMainLooper()).postDelayed(
                () -> runUpdateCheck(false), 3000);
    }

    private void runUpdateCheck(boolean manual) {
        getSharedPreferences("update_prefs", MODE_PRIVATE).edit()
                .putLong("last_check_ts", System.currentTimeMillis()).apply();

        UpdateChecker.check(this, BuildConfig.VERSION_NAME, new UpdateChecker.UpdateCallback() {
            @Override
            public void onUpdateAvailable(String tagName, String downloadUrl,
                                          String releaseNotes, String releasePageUrl) {
                showUpdateDialog(tagName, downloadUrl, releaseNotes, releasePageUrl);
            }
            @Override
            public void onUpToDate() {
                if (manual) {
                    Snackbar.make(binding.getRoot(),
                            "✓ Aplikasi sudah versi terbaru (" + BuildConfig.VERSION_NAME + ")",
                            Snackbar.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onError(String message) {
                if (manual) {
                    Snackbar.make(binding.getRoot(),
                            "Gagal cek update. Periksa koneksi internet.",
                            Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showUpdateDialog(String tagName, String downloadUrl,
                                  String releaseNotes, String releasePageUrl) {
        String notes = (releaseNotes != null && !releaseNotes.trim().isEmpty())
                ? releaseNotes.trim() : "Versi baru tersedia. Download sekarang!";
        // Potong release notes jika terlalu panjang
        if (notes.length() > 400) notes = notes.substring(0, 397) + "...";

        String msg = "Versi baru: " + tagName + "\n"
                + "Versi kamu: v" + BuildConfig.VERSION_NAME + "\n\n"
                + notes;

        String finalNotes = notes;
        new AlertDialog.Builder(this)
                .setTitle("🎉 Update Tersedia!")
                .setMessage(msg)
                .setCancelable(true)
                .setPositiveButton("⬇ Download & Install", (d, w) -> {
                    if (downloadUrl != null && !downloadUrl.isEmpty()) {
                        startAPKDownload(downloadUrl, tagName);
                    } else {
                        // Fallback: buka browser ke release page
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(releasePageUrl)));
                    }
                })
                .setNeutralButton("🌐 Lihat di GitHub", (d, w) ->
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(releasePageUrl))))
                .setNegativeButton("Nanti", null)
                .show();
    }

    private void startAPKDownload(String downloadUrl, String tagName) {
        Snackbar.make(binding.getRoot(),
                "⬇ Mengunduh " + tagName + "... Lihat notifikasi",
                Snackbar.LENGTH_LONG).show();

        APKDownloader.download(this, downloadUrl, tagName, new APKDownloader.DownloadCallback() {
            @Override
            public void onComplete(long downloadId) {
                DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                APKDownloader.installFromDownloadManager(MainActivity.this, dm, downloadId);
            }
            @Override
            public void onFailed() {
                runOnUiThread(() -> Snackbar.make(binding.getRoot(),
                        "❌ Download gagal. Coba dari GitHub.",
                        Snackbar.LENGTH_LONG).show());
            }
        });
    }

    // ─── Setup ────────────────────────────────────────────────────────────────

    private void checkTermuxStatus() {
        boolean installed = TermuxConnector.isTermuxInstalled(this);
        if (!installed) {
            binding.statusCard.setVisibility(View.VISIBLE);
            binding.statusIcon.setImageResource(R.drawable.ic_error);
            binding.statusText.setText("Termux tidak ditemukan. Install Termux dari F-Droid.");
            binding.statusCard.setCardBackgroundColor(getColor(R.color.error_color));
        } else {
            binding.statusCard.setVisibility(View.VISIBLE);
            binding.statusIcon.setImageResource(R.drawable.ic_check);
            binding.statusText.setText("Termux terdeteksi ✓ — Pastikan allow-external-apps=true");
            binding.statusCard.setCardBackgroundColor(getColor(R.color.success_color));
            new Handler(Looper.getMainLooper()).postDelayed(
                    () -> binding.statusCard.setVisibility(View.GONE), 3000);
        }
    }

    private void setupRecyclerView() {
        adapter = new PackageAdapter(pkg -> {
            Intent detail = new Intent(this, PackageDetailActivity.class);
            detail.putExtra("pkg_name", pkg.getName());
            detail.putExtra("pkg_version", pkg.getVersion());
            detail.putExtra("pkg_installed", pkg.isInstalled());
            startActivity(detail);
        });
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
    }

    private void restoreTab() {
        suppressTabEvent = true;
        TabLayout.Tab t = binding.tabLayout.getTabAt(lastRealTab);
        if (t != null) t.select();
        suppressTabEvent = false;
        binding.searchLayout.setVisibility(lastRealTab == 0 ? View.VISIBLE : View.GONE);
        binding.btnUpdate.setVisibility(lastRealTab == 0 ? View.VISIBLE : View.GONE);
    }

    private void setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Search"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Installed"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Scripts"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Console"));

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (suppressTabEvent) return;
                switch (tab.getPosition()) {
                    case 0:
                        lastRealTab = 0;
                        binding.searchLayout.setVisibility(View.VISIBLE);
                        binding.btnUpdate.setVisibility(View.VISIBLE);
                        loadSearch(binding.searchInput.getText().toString());
                        break;
                    case 1:
                        lastRealTab = 1;
                        binding.searchLayout.setVisibility(View.GONE);
                        binding.btnUpdate.setVisibility(View.GONE);
                        loadInstalledPackages();
                        break;
                    case 2:
                        restoreTab();
                        startActivity(new Intent(MainActivity.this, ScriptRepoActivity.class));
                        break;
                    case 3:
                        restoreTab();
                        startActivity(new Intent(MainActivity.this, ConsoleActivity.class));
                        break;
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSearch() {
        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int i, int b, int c) {}
            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                debounceHandler.removeCallbacks(searchRunnable);
                if (query.isEmpty()) {
                    adapter.updateList(new ArrayList<>());
                    binding.emptyState.setVisibility(View.GONE);
                    return;
                }
                if (query.length() < 2) return;
                searchRunnable = () -> loadSearch(query);
                debounceHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE);
            }
        });
        binding.btnSearch.setOnClickListener(v -> {
            String q = binding.searchInput.getText().toString().trim();
            if (!q.isEmpty()) {
                debounceHandler.removeCallbacks(searchRunnable);
                loadSearch(q);
            }
        });
    }

    private void setupButtons() {
        binding.btnUpdate.setOnClickListener(v ->
                showConfirmDialog("Update Repository?",
                        "Jalankan apt update untuk refresh daftar package?", () -> {
                            currentCommand = CMD_UPDATE;
                            showLoading(true, "Mengupdate repository...");
                            TermuxConnector.aptUpdate(this, buildPendingIntent());
                        }));

        binding.swipeRefresh.setOnRefreshListener(() -> {
            if (lastRealTab == 0) loadSearch(binding.searchInput.getText().toString());
            else if (lastRealTab == 1) loadInstalledPackages();
            else binding.swipeRefresh.setRefreshing(false);
        });
    }

    // ─── Commands ─────────────────────────────────────────────────────────────

    private void loadSearch(String query) {
        if (query.isEmpty()) {
            adapter.updateList(new ArrayList<>());
            binding.emptyState.setVisibility(View.GONE);
            return;
        }
        currentCommand = CMD_SEARCH;
        showLoading(true, "Mencari package '" + query + "'...");
        TermuxConnector.aptSearch(this, query, buildPendingIntent());
    }

    private void loadInstalledPackages() {
        currentCommand = CMD_INSTALLED;
        showLoading(true, "Memuat package terinstall...");
        TermuxConnector.aptListInstalled(this, buildPendingIntent());
    }

    private PendingIntent buildPendingIntent() {
        Intent intent = new Intent(ACTION_RESULT);
        intent.setPackage(getPackageName());
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) flags |= PendingIntent.FLAG_MUTABLE;
        return PendingIntent.getBroadcast(this, 0, intent, flags);
    }

    // ─── Result Handling ─────────────────────────────────────────────────────

    private void handleResult(String stdout, String stderr, int exitCode) {
        showLoading(false, null);
        binding.swipeRefresh.setRefreshing(false);

        if (currentCommand == CMD_UPDATE) {
            Snackbar.make(binding.getRoot(),
                    exitCode == 0 ? "Repository diupdate! ✓" : "Update gagal. Cek koneksi internet.",
                    Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (currentCommand == CMD_UPGRADE) {
            Snackbar.make(binding.getRoot(),
                    exitCode == 0 ? "Upgrade selesai! ✓" : "Upgrade selesai dengan beberapa error.",
                    Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (currentCommand == CMD_STORAGE) {
            new AlertDialog.Builder(this)
                    .setTitle("💾 Info Storage")
                    .setMessage(stdout.isEmpty() ? "Tidak ada data storage." : stdout)
                    .setPositiveButton("OK", null).show();
            return;
        }

        List<Package> newList = new ArrayList<>();

        if (currentCommand == CMD_SEARCH) {
            String[] lines = stdout.split("\n");
            Package current = null;
            for (String line : lines) {
                if ((line.startsWith("  ") || line.startsWith("\t")) && current != null) {
                    current.setDescription(line.trim());
                    newList.add(current);
                    current = null;
                } else {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty()
                            || trimmed.startsWith("WARNING")
                            || trimmed.startsWith("NOTE")) continue;
                    if (current != null) newList.add(current);
                    current = Package.parseFromAptSearch(trimmed);
                }
            }
            if (current != null) newList.add(current);
        } else if (currentCommand == CMD_INSTALLED) {
            for (String line : stdout.split("\n")) {
                Package pkg = Package.parseFromDpkg(line);
                if (pkg != null) newList.add(pkg);
            }
        }

        adapter.updateList(newList);

        if (newList.isEmpty()) {
            showEmptyState(currentCommand == CMD_INSTALLED
                    ? "Tidak ada package terinstall ditemukan."
                    : "Tidak ada package ditemukan untuk pencarian ini.");
        } else {
            binding.emptyState.setVisibility(View.GONE);
        }
    }

    // ─── UI Helpers ──────────────────────────────────────────────────────────

    private void showLoading(boolean show, String message) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.loadingText.setVisibility(show ? View.VISIBLE : View.GONE);
        if (message != null) binding.loadingText.setText(message);
        binding.recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        if (show) binding.emptyState.setVisibility(View.GONE);
    }

    private void showEmptyState(String msg) {
        binding.emptyState.setVisibility(View.VISIBLE);
        binding.emptyStateText.setText(msg);
    }

    private void showConfirmDialog(String title, String msg, Runnable onConfirm) {
        new AlertDialog.Builder(this)
                .setTitle(title).setMessage(msg)
                .setPositiveButton("Ya", (d, w) -> onConfirm.run())
                .setNegativeButton("Batal", null).show();
    }

    // ─── Menu ────────────────────────────────────────────────────────────────

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_upgrade) {
            showConfirmDialog("Upgrade Semua Package?",
                    "Jalankan apt upgrade -y? Proses ini bisa memakan waktu.", () -> {
                        currentCommand = CMD_UPGRADE;
                        showLoading(true, "Upgrading packages...");
                        TermuxConnector.aptUpgrade(this, buildPendingIntent());
                    });
            return true;
        } else if (id == R.id.action_storage) {
            currentCommand = CMD_STORAGE;
            showLoading(true, "Memuat info storage...");
            TermuxConnector.getStorageInfo(this, buildPendingIntent());
            return true;
        } else if (id == R.id.action_check_update) {
            Snackbar.make(binding.getRoot(), "Memeriksa update...", Snackbar.LENGTH_SHORT).show();
            runUpdateCheck(true);
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_about) {
            showAbout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAbout() {
        new AlertDialog.Builder(this)
                .setTitle("TermuxIwx v" + BuildConfig.VERSION_NAME)
                .setMessage("Package manager & toolbox untuk Termux.\n\n"
                        + "Dibuat oleh Kztutorial99\n"
                        + "GitHub: github.com/Kztutorial99/TermuxIwx\n\n"
                        + "Pastikan:\n"
                        + "• Termux dari F-Droid (bukan Play Store)\n"
                        + "• allow-external-apps=true di termux.properties\n"
                        + "• Permission RUN_COMMAND granted di Termux")
                .setPositiveButton("OK", null)
                .setNeutralButton("🔄 Cek Update", (d, w) -> runUpdateCheck(true))
                .show();
    }
}
