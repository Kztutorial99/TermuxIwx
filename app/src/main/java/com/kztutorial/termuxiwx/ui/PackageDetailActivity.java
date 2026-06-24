package com.kztutorial.termuxiwx.ui;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.kztutorial.termuxiwx.databinding.ActivityPackageDetailBinding;
import com.kztutorial.termuxiwx.utils.TermuxConnector;

public class PackageDetailActivity extends AppCompatActivity {

    private static final String ACTION_RESULT = "com.kztutorial.termuxiwx.PKG_RESULT";
    private ActivityPackageDetailBinding binding;
    private String pkgName;
    private boolean isInstalled;
    private int pendingAction = 0;
    private static final int ACTION_INSTALL = 1;
    private static final int ACTION_REMOVE  = 2;
    private static final int ACTION_INFO    = 3;
    private static final int ACTION_PURGE   = 4;

    private final BroadcastReceiver resultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            Bundle extras = intent.getExtras();
            Bundle resultBundle = extras != null ? extras.getBundle("result") : null;
            String stdout = resultBundle != null ? resultBundle.getString("stdout", "") : "";
            int exitCode = resultBundle != null ? resultBundle.getInt("exitCode", -1) : -1;
            runOnUiThread(() -> handleResult(stdout, exitCode));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPackageDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        pkgName = getIntent().getStringExtra("pkg_name");
        String pkgVersion = getIntent().getStringExtra("pkg_version");
        isInstalled = getIntent().getBooleanExtra("pkg_installed", false);

        binding.pkgName.setText(pkgName);
        binding.pkgVersion.setText("v" + (pkgVersion != null ? pkgVersion : "-"));
        binding.pkgStatus.setText(isInstalled ? "\u2713 Terinstall" : "Belum terinstall");
        binding.pkgStatus.setTextColor(getColor(isInstalled ?
                com.kztutorial.termuxiwx.R.color.success_color :
                com.kztutorial.termuxiwx.R.color.warning_color));

        updateButtons();
        loadPackageInfo();

        binding.btnInstall.setOnClickListener(v -> {
            if (isInstalled) {
                showConfirmDialog("Hapus Package?",
                        "Hapus " + pkgName + "?\n\nConfig file akan tetap tersimpan.", () -> {
                            pendingAction = ACTION_REMOVE;
                            showLoading(true);
                            TermuxConnector.aptRemove(this, pkgName, buildPendingIntent());
                        });
            } else {
                showConfirmDialog("Install Package?",
                        "Install " + pkgName + "?", () -> {
                            pendingAction = ACTION_INSTALL;
                            showLoading(true);
                            TermuxConnector.aptInstall(this, pkgName, buildPendingIntent());
                        });
            }
        });

        binding.btnPurge.setOnClickListener(v ->
            showConfirmDialog("Hapus + Bersihkan Config?",
                "Purge " + pkgName + "?\n\nIni akan menghapus package BESERTA semua file konfigurasinya. Tidak dapat dikembalikan.",
                () -> {
                    pendingAction = ACTION_PURGE;
                    showLoading(true);
                    TermuxConnector.aptPurge(this, pkgName, buildPendingIntent());
                }));

        binding.btnOpenConsole.setOnClickListener(v -> {
            Intent console = new Intent(this, ConsoleActivity.class);
            console.putExtra("initial_cmd", pkgName + " --help 2>&1 | head -50");
            startActivity(console);
        });
    }

    private void loadPackageInfo() {
        pendingAction = ACTION_INFO;
        TermuxConnector.pkgInfo(this, pkgName, buildPendingIntent());
    }

    private void updateButtons() {
        binding.btnInstall.setText(isInstalled ? "\ud83d\uddd1 Hapus Package" : "\u26a1 Install Sekarang");
        binding.btnInstall.setBackgroundTintList(getColorStateList(isInstalled ?
                com.kztutorial.termuxiwx.R.color.error_color :
                com.kztutorial.termuxiwx.R.color.colorPrimary));
        binding.btnPurge.setVisibility(isInstalled ? View.VISIBLE : View.GONE);
    }

    private void handleResult(String stdout, int exitCode) {
        showLoading(false);
        if (pendingAction == ACTION_INSTALL) {
            if (exitCode == 0) {
                isInstalled = true;
                updateButtons();
                binding.pkgStatus.setText("\u2713 Terinstall");
                binding.pkgStatus.setTextColor(getColor(com.kztutorial.termuxiwx.R.color.success_color));
                Snackbar.make(binding.getRoot(), pkgName + " berhasil diinstall! \u2713", Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(binding.getRoot(), "Gagal install " + pkgName + ". Buka Console untuk detail.", Snackbar.LENGTH_LONG)
                    .setAction("Buka Console", v -> {
                        Intent console = new Intent(this, ConsoleActivity.class);
                        console.putExtra("initial_cmd", "apt install -y " + pkgName);
                        startActivity(console);
                    }).show();
            }
        } else if (pendingAction == ACTION_REMOVE) {
            if (exitCode == 0) {
                isInstalled = false;
                updateButtons();
                binding.pkgStatus.setText("Belum terinstall");
                binding.pkgStatus.setTextColor(getColor(com.kztutorial.termuxiwx.R.color.warning_color));
                Snackbar.make(binding.getRoot(), pkgName + " berhasil dihapus.", Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(binding.getRoot(), "Gagal hapus " + pkgName + ". Buka Console untuk detail.", Snackbar.LENGTH_LONG)
                    .setAction("Buka Console", v -> startActivity(new Intent(this, ConsoleActivity.class))).show();
            }
        } else if (pendingAction == ACTION_PURGE) {
            if (exitCode == 0) {
                isInstalled = false;
                updateButtons();
                binding.pkgStatus.setText("Belum terinstall");
                binding.pkgStatus.setTextColor(getColor(com.kztutorial.termuxiwx.R.color.warning_color));
                Snackbar.make(binding.getRoot(), pkgName + " + config berhasil dihapus (purge). \u2713", Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(binding.getRoot(), "Gagal purge " + pkgName + ". Buka Console untuk detail.", Snackbar.LENGTH_LONG)
                    .setAction("Buka Console", v -> startActivity(new Intent(this, ConsoleActivity.class))).show();
            }
        } else if (pendingAction == ACTION_INFO) {
            binding.pkgInfo.setText(stdout.isEmpty() ? "Tidak ada info tersedia." : stdout);
        }
    }

    private PendingIntent buildPendingIntent() {
        Intent intent = new Intent(ACTION_RESULT);
        intent.setPackage(getPackageName());
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) flags |= PendingIntent.FLAG_MUTABLE;
        return PendingIntent.getBroadcast(this, 1, intent, flags);
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnInstall.setEnabled(!show);
        binding.btnPurge.setEnabled(!show);
    }

    private void showConfirmDialog(String title, String msg, Runnable onConfirm) {
        new AlertDialog.Builder(this)
            .setTitle(title).setMessage(msg)
            .setPositiveButton("Ya", (d, w) -> onConfirm.run())
            .setNegativeButton("Batal", null).show();
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
        try { unregisterReceiver(resultReceiver); } catch (Exception ignored) {}
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
