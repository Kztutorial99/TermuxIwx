package com.kztutorial.termuxiwx.ui;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;

import com.kztutorial.termuxiwx.databinding.ActivityConsoleBinding;
import com.kztutorial.termuxiwx.utils.TermuxConnector;

public class ConsoleActivity extends AppCompatActivity {

    private static final String ACTION_RESULT = "com.kztutorial.termuxiwx.CONSOLE_RESULT";
    private ActivityConsoleBinding binding;
    private StringBuilder outputBuffer = new StringBuilder();

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

        binding.consoleOutput.setText("TermuxIwx Console v1.0\nKetik perintah dan tekan Enter atau tombol Run\n\n$ ");

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
            binding.consoleOutput.setText("Console cleared.\n\n$ ");
        });

        String initialCmd = getIntent().getStringExtra("initial_cmd");
        if (initialCmd != null && !initialCmd.isEmpty()) {
            binding.cmdInput.setText(initialCmd);
            runCommand();
        }
    }

    private void runCommand() {
        String cmd = binding.cmdInput.getText().toString().trim();
        if (cmd.isEmpty()) return;

        outputBuffer.append("$ ").append(cmd).append("\n");
        binding.consoleOutput.setText(outputBuffer.toString());
        binding.cmdInput.setText("");
        binding.progressBar.setVisibility(View.VISIBLE);

        TermuxConnector.customCommand(this, cmd, buildPendingIntent());
        scrollToBottom();
    }

    private void appendOutput(String stdout, String stderr, int exitCode) {
        binding.progressBar.setVisibility(View.GONE);
        if (!stdout.isEmpty()) outputBuffer.append(stdout).append("\n");
        if (!stderr.isEmpty()) outputBuffer.append("[stderr] ").append(stderr).append("\n");
        outputBuffer.append("[exit: ").append(exitCode).append("]\n$ ");
        binding.consoleOutput.setText(outputBuffer.toString());
        scrollToBottom();
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
        try { unregisterReceiver(resultReceiver); } catch (Exception ignored) {}
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
