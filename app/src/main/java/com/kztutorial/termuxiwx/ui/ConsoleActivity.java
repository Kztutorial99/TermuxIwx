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
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.kztutorial.termuxiwx.R;
import com.kztutorial.termuxiwx.databinding.ActivityConsoleBinding;
import com.kztutorial.termuxiwx.utils.TermuxConnector;

public class ConsoleActivity extends AppCompatActivity {

    private static final String ACTION_RESULT = "com.kztutorial.termuxiwx.CONSOLE_RESULT";
    private ActivityConsoleBinding binding;
    private final StringBuilder outputBuffer = new StringBuilder();
    private boolean isRunning = false;
    private String lastCommand = "";
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private long startTime = 0;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isRunning) return;
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            binding.runningStatus.setText("⏳ Menjalankan... " + elapsed + "s");
            timerHandler.postDelayed(this, 1000);
        }
    };

    private final BroadcastReceiver resultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            Bundle extras = intent.getExtras();
            Bundle resultBundle = extras != null ? extras.getBundle("result") : null;
            String stdout = resultBundle != null ? resultBundle.getString("stdout", "") : "";
            String stderr = resultBundle != null ? resultBundle.getString("stderr", "") : "";
            int exitCode = resultBundle != null ? resultBundle.getInt("exitCode", -1) : -1;
            runOnUiThread(() -> appendOutput(stdout, stderr, exitCode));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConsoleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Console");

        outputBuffer.append("TermuxIwx Console v1.0\n");
        outputBuffer.append("Ketik perintah lalu tekan Run atau Enter\n");
        outputBuffer.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        binding.consoleOutput.setText(outputBuffer.toString());

        binding.cmdInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                runCommand();
                return true;
            }
            return false;
        });

        binding.btnRun.setOnClickListener(v -> runCommand());
        binding.btnClear.setOnClickListener(v -> {
            outputBuffer.setLength(0);
            outputBuffer.append("Console cleared.\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            binding.consoleOutput.setText(outputBuffer.toString());
        });

        // Quick command chips
        binding.chipLs.setOnClickListener(v -> { binding.cmdInput.setText("ls"); runCommand(); });
        binding.chipPkgUpdate.setOnClickListener(v -> { binding.cmdInput.setText("pkg update"); runCommand(); });
        binding.chipPwd.setOnClickListener(v -> { binding.cmdInput.setText("pwd"); runCommand(); });
        binding.chipDf.setOnClickListener(v -> { binding.cmdInput.setText("df -h"); runCommand(); });
        binding.chipTop.setOnClickListener(v -> { binding.cmdInput.setText("pkg list-installed 2>/dev/null | wc -l"); runCommand(); });
        binding.chipUname.setOnClickListener(v -> { binding.cmdInput.setText("uname -a"); runCommand(); });

        String initialCmd = getIntent().getStringExtra("initial_cmd");
        if (initialCmd != null && !initialCmd.isEmpty()) {
            binding.cmdInput.setText(initialCmd);
            runCommand();
        }
    }

    private void runCommand() {
        String cmd = binding.cmdInput.getText().toString().trim();
        if (cmd.isEmpty()) return;
        if (isRunning) {
            Toast.makeText(this, "Tunggu perintah sebelumnya selesai...", Toast.LENGTH_SHORT).show();
            return;
        }

        lastCommand = cmd;
        isRunning = true;

        // Tampilkan prompt + command di buffer (TANPA trailing $ di sini)
        outputBuffer.append("\n$ ").append(cmd).append("\n");
        binding.consoleOutput.setText(outputBuffer.toString());
        binding.cmdInput.setText("");

        // Disable input saat berjalan
        binding.cmdInput.setEnabled(false);
        binding.btnRun.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.runningStatus.setVisibility(View.VISIBLE);
        binding.runningStatus.setText("⏳ Menjalankan...");

        // Mulai timer
        startTime = System.currentTimeMillis();
        timerHandler.post(timerRunnable);

        TermuxConnector.customCommand(this, cmd, buildPendingIntent());
        scrollToBottom();
    }

    private void appendOutput(String stdout, String stderr, int exitCode) {
        // Stop timer & re-enable input
        timerHandler.removeCallbacks(timerRunnable);
        isRunning = false;
        binding.progressBar.setVisibility(View.GONE);
        binding.runningStatus.setVisibility(View.GONE);
        binding.cmdInput.setEnabled(true);
        binding.btnRun.setEnabled(true);

        long elapsed = (System.currentTimeMillis() - startTime) / 1000;

        // Tulis stdout
        if (!stdout.isEmpty()) {
            outputBuffer.append(stdout);
            if (!stdout.endsWith("\n")) outputBuffer.append("\n");
        }

        // Filter & tulis stderr (buang noise APT warning yang berulang)
        String filteredStderr = filterStderr(stderr);
        if (!filteredStderr.isEmpty()) {
            outputBuffer.append("[!] ").append(filteredStderr);
            if (!filteredStderr.endsWith("\n")) outputBuffer.append("\n");
        }

        // Exit code — tampilkan hanya jika error
        if (exitCode == 0) {
            outputBuffer.append("✓ selesai (").append(elapsed).append("s)\n");
        } else {
            outputBuffer.append("✗ exit: ").append(exitCode).append(" (").append(elapsed).append("s)\n");
        }

        binding.consoleOutput.setText(outputBuffer.toString());
        scrollToBottom();

        // Deteksi command not found & tawarkan install
        detectAndSuggestInstall(stdout, stderr, exitCode);

        // Re-enable input field
        binding.cmdInput.requestFocus();
    }

    private String filterStderr(String stderr) {
        if (stderr == null || stderr.trim().isEmpty()) return "";

        String[] noisePatterns = {
            "WARNING: apt does not have a stable CLI interface",
            "Use with caution in scripts",
            "debconf: delaying package configuration",
        };

        StringBuilder filtered = new StringBuilder();
        for (String line : stderr.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            boolean isNoise = false;
            for (String noise : noisePatterns) {
                if (trimmed.contains(noise)) { isNoise = true; break; }
            }
            if (!isNoise) {
                filtered.append(trimmed).append("\n");
            }
        }
        return filtered.toString().trim();
    }

    private void detectAndSuggestInstall(String stdout, String stderr, int exitCode) {
        if (exitCode == 0) return;

        String combined = (stdout + " " + stderr).toLowerCase();
        boolean isNotFound = combined.contains("not found")
            || combined.contains("no such file or directory")
            || combined.contains("command not found")
            || combined.contains("is not installed");

        if (!isNotFound) return;

        // Coba extract nama package dari command yang diketik
        String baseCmd = lastCommand.trim().split("\\s+")[0];
        // Strip path if any
        if (baseCmd.contains("/")) baseCmd = baseCmd.substring(baseCmd.lastIndexOf('/') + 1);

        // Beberapa mapping command → package name
        String pkgName = mapCommandToPackage(baseCmd);

        String finalPkgName = pkgName;
        new AlertDialog.Builder(this)
            .setTitle("⚠ Perintah Tidak Ditemukan")
            .setMessage("\"" + baseCmd + "\" belum terinstall di Termux.\n\nInstall sekarang via:\npkg install " + finalPkgName)
            .setPositiveButton("⚡ Install Sekarang", (d, w) -> {
                binding.cmdInput.setText("pkg install -y " + finalPkgName);
                runCommand();
            })
            .setNegativeButton("Batal", null)
            .show();
    }

    private String mapCommandToPackage(String cmd) {
        switch (cmd) {
            case "wget": return "wget";
            case "curl": return "curl";
            case "git": return "git";
            case "python": case "python3": return "python";
            case "node": case "nodejs": return "nodejs";
            case "php": return "php";
            case "ruby": return "ruby";
            case "gcc": return "clang";
            case "make": return "make";
            case "nmap": return "nmap";
            case "ssh": return "openssh";
            case "ffmpeg": return "ffmpeg";
            case "zip": return "zip";
            case "unzip": return "unzip";
            case "tar": return "tar";
            case "vim": return "vim";
            case "nano": return "nano";
            case "htop": return "htop";
            case "ping": return "inetutils";
            case "netstat": return "net-tools";
            case "ifconfig": return "net-tools";
            case "ip": return "iproute2";
            case "java": return "openjdk-17";
            case "zsh": return "zsh";
            case "fish": return "fish";
            case "tmux": return "tmux";
            case "screen": return "screen";
            case "sl": return "sl";
            case "neofetch": return "neofetch";
            default: return cmd;
        }
    }

    private void scrollToBottom() {
        binding.consoleScroll.post(() ->
            binding.consoleScroll.fullScroll(View.FOCUS_DOWN));
    }

    private PendingIntent buildPendingIntent() {
        Intent intent = new Intent(ACTION_RESULT);
        intent.setPackage(getPackageName());
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) flags |= PendingIntent.FLAG_MUTABLE;
        return PendingIntent.getBroadcast(this, 2, intent, flags);
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
        timerHandler.removeCallbacks(timerRunnable);
        try { unregisterReceiver(resultReceiver); } catch (Exception ignored) {}
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
