package com.kztutorial.termuxiwx.ui;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.kztutorial.termuxiwx.R;
import com.kztutorial.termuxiwx.databinding.ActivityMainBinding;
import com.kztutorial.termuxiwx.models.Package;
import com.kztutorial.termuxiwx.ui.adapters.PackageAdapter;
import com.kztutorial.termuxiwx.utils.TermuxConnector;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String ACTION_RESULT = "com.kztutorial.termuxiwx.RESULT";
    private static final int CMD_SEARCH   = 1;
    private static final int CMD_INSTALLED = 2;
    private static final int CMD_UPDATE   = 3;
    private static final int CMD_UPGRADE  = 4;
    private static final int CMD_STORAGE  = 5;

    private static final long SEARCH_DEBOUNCE_MS = 500;

    private ActivityMainBinding binding;
    private PackageAdapter adapter;
    private int currentCommand = 0;

    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    private final BroadcastReceiver resultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            Bundle extras = intent.getExtras();
            if (extras == null) return;
            Bundle resultBundle = extras.getBundle("result");
            String stdout = resultBundle != null ? resultBundle.getString("stdout", "") : "";
            String stderr = resultBundle != null ? resultBundle.getString("stderr", "") : "";
            int exitCode  = resultBundle != null ? resultBundle.getInt("exitCode", -1) : -1;
            runOnUiThread(() -> handleResult(stdout, stderr, exitCode));
        }
    };

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
    }

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

    private void setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Search"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Installed"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Scripts"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Console"));

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        binding.searchLayout.setVisibility(View.VISIBLE);
                        loadSearch(binding.searchInput.getText().toString());
                        break;
                    case 1:
                        binding.searchLayout.setVisibility(View.GONE);
                        loadInstalledPackages();
                        break;
                    case 2:
                        binding.searchLayout.setVisibility(View.GONE);
                        startActivity(new Intent(MainActivity.this, ScriptRepoActivity.class));
                        binding.tabLayout.post(() -> {
                            TabLayout.Tab t = binding.tabLayout.getTabAt(0);
                            if (t != null) t.select();
                        });
                        break;
                    case 3:
                        startActivity(new Intent(MainActivity.this, ConsoleActivity.class));
                        binding.tabLayout.post(() -> {
                            TabLayout.Tab t = binding.tabLayout.getTabAt(0);
                            if (t != null) t.select();
                        });
                        break;
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSearch() {
        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
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
                debounceHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_MS);
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
            int tab = binding.tabLayout.getSelectedTabPosition();
            if (tab == 0) loadSearch(binding.searchInput.getText().toString());
            else if (tab == 1) loadInstalledPackages();
            else binding.swipeRefresh.setRefreshing(false);
        });
    }

    private void loadSearch(String query) {
        currentCommand = CMD_SEARCH;
        if (query.isEmpty()) {
            adapter.updateList(new ArrayList<>());
            return;
        }
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

    private void handleResult(String stdout, String stderr, int exitCode) {
        showLoading(false, null);
        binding.swipeRefresh.setRefreshing(false);

        if (currentCommand == CMD_UPDATE) {
            if (exitCode == 0) {
                Snackbar.make(binding.getRoot(), "Repository diupdate! ✓", Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(binding.getRoot(), "Update gagal. Cek koneksi internet.", Snackbar.LENGTH_LONG).show();
            }
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
                .setPositiveButton("OK", null)
                .show();
            return;
        }

        List<Package> newList = new ArrayList<>();

        if (currentCommand == CMD_SEARCH) {
            for (String line : stdout.split("\n")) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("WARNING") || trimmed.startsWith("NOTE")) continue;
                Package pkg = Package.parseFromAptSearch(trimmed);
                if (pkg != null) newList.add(pkg);
            }
        } else if (currentCommand == CMD_INSTALLED) {
            for (String line : stdout.split("\n")) {
                Package pkg = Package.parseFromDpkg(line);
                if (pkg != null) newList.add(pkg);
            }
        }

        adapter.updateList(newList);

        if (newList.isEmpty() && exitCode != 0) {
            showEmptyState("Tidak ada hasil. " + (stderr.isEmpty() ? "" : stderr.split("\n")[0]));
        } else if (newList.isEmpty()) {
            showEmptyState(currentCommand == CMD_INSTALLED
                ? "Tidak ada package terinstall ditemukan."
                : "Tidak ada package ditemukan.");
        } else {
            binding.emptyState.setVisibility(View.GONE);
        }
    }

    private void showLoading(boolean show, String message) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.loadingText.setVisibility(show ? View.VISIBLE : View.GONE);
        if (message != null) binding.loadingText.setText(message);
        binding.recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(String msg) {
        binding.emptyState.setVisibility(View.VISIBLE);
        binding.emptyStateText.setText(msg);
    }

    private void showConfirmDialog(String title, String msg, Runnable onConfirm) {
        new AlertDialog.Builder(this)
            .setTitle(title).setMessage(msg)
            .setPositiveButton("Ya", (d, w) -> onConfirm.run())
            .setNegativeButton("Batal", null)
            .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ACTION_RESULT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(resultReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(resultReceiver, filter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        debounceHandler.removeCallbacks(searchRunnable);
        try { unregisterReceiver(resultReceiver); } catch (Exception ignored) {}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_upgrade) {
            showConfirmDialog("Upgrade Semua Package?",
                "Jalankan apt upgrade -y? Proses ini bisa memakan waktu.", () -> {
                    currentCommand = CMD_UPGRADE;
                    showLoading(true, "Upgrading packages...");
                    TermuxConnector.aptUpgrade(this, buildPendingIntent());
                });
            return true;
        } else if (item.getItemId() == R.id.action_storage) {
            currentCommand = CMD_STORAGE;
            showLoading(true, "Memuat info storage...");
            TermuxConnector.getStorageInfo(this, buildPendingIntent());
            return true;
        } else if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_about) {
            showAbout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAbout() {
        new AlertDialog.Builder(this)
            .setTitle("TermuxIwx v1.5.0")
            .setMessage("Package manager & toolbox untuk Termux.\n\nDibuat oleh Kztutorial99\n\nPastikan:\n• Termux terinstall dari F-Droid (bukan Play Store)\n• allow-external-apps=true di termux.properties\n• Permission RUN_COMMAND granted di Termux")
            .setPositiveButton("OK", null)
            .show();
    }
}
