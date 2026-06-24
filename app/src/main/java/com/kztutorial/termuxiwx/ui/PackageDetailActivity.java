package com.kztutorial.termuxiwx.ui;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.kztutorial.termuxiwx.databinding.ActivityPackageDetailBinding;
import com.kztutorial.termuxiwx.utils.TermuxConnector;

public class PackageDetailActivity extends AppCompatActivity {

    private static final String ACTION_RESULT = "com.kztutorial.termuxiwx.PKG_RESULT";
    private ActivityPackageDetailBinding binding;
    private String pkgName;
    private boolean isInstalled;
    private int pendingAction = 0;
    private static final int ACTION_INSTALL = 1;
    private static final int ACTION_REMOVE = 2;
    private static final int ACTION_INFO = 3;

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

        pkgName = getIntent().getStringExtra("pkg_name");
        String pkgVersion = getIntent().getStringExtra("pkg_version");
        isInstalled = getIntent().getBooleanExtra("pkg_installed", false);

        binding.pkgName.setText(pkgName);
        binding.pkgVersion.setText("Versi: " + (pkgVersion != null ? pkgVersion : "-"));
        binding.pkgStatus.setText(isInstalled ? "✓ Terinstall" : "Belum terinstall");
        binding.pkgStatus.setTextColor(getColor(isInstalled ?
                com.kztutorial.termuxiwx.R.color.success_color :
                com.kztutorial.termuxiwx.R.color.warning_color));

        updateButtons();
        loadPackageInfo();

        binding.btnInstall.setOnClickListener(v -> {
            if (isInstalled) {
                showConfirmDialog("Hapus Package?",
                        "Hapus " + pkgName + "?", () -> {
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
        binding.btnInstall.setText(isInstalled ? "🗑 Hapus Package" : "⚡ Install Sekarang");
        binding.btnInstall.setBackgroundTintList(getColorStateList(isInstalled ?
                com.kztutorial.termuxiwx.R.color.error_color :
                com.kztutorial.termuxiwx.R.color.colorPrimary));
    }

    private void handleResult(String stdout, int exitCode) {
        showLoading(false);
        if (pendingAction == ACTION_INSTALL) {
            if (exitCode == 0) {
                isInstalled = true;
                updateButtons();
                binding.pkgStatus.setText("✓ Terinstall");
                binding.pkgStatus.setTextColor(getColor(com.kztutorial.termuxiwx.R.color.success_color));
                Toast.makeText(this, pkgName + " berhasil diinstall! ✓", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Gagal install " + pkgName, Toast.LENGTH_LONG).show();
            }
        } else if (pendingAction == ACTION_REMOVE) {
            if (exitCode == 0) {
                isInstalled = false;
                updateButtons();
                binding.pkgStatus.setText("Belum terinstall");
                binding.pkgStatus.setTextColor(getColor(com.kztutorial.termuxiwx.R.color.warning_color));
                Toast.makeText(this, pkgName + " berhasil dihapus.", Toast.LENGTH_SHORT).show();
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
    }

    private void showConfirmDialog(String title, String msg, Runnable onConfirm) {
        new AlertDialog.Builder(this)
            .setTitle(title).setMessage(msg)
            .setPositiveButton("Ya", (d, w) -> onConfirm.run())
            .setNegativeButton("Batal", null).show();
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
        try { unregisterReceiver(resultReceiver); } catch (Exception ignored) {}
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
