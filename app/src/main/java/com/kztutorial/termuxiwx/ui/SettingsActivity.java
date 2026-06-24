package com.kztutorial.termuxiwx.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.kztutorial.termuxiwx.databinding.ActivitySettingsBinding;
import com.kztutorial.termuxiwx.utils.AppSettings;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private AppSettings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Pengaturan");
        }

        settings = new AppSettings(this);
        loadCurrentSettings();
        setupListeners();
    }

    private void loadCurrentSettings() {
        binding.inputTermuxPath.setText(settings.getTermuxPath());

        String[] shells = {"bash", "zsh", "fish", "sh"};
        ArrayAdapter<String> shellAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, shells);
        binding.spinnerShell.setAdapter(shellAdapter);
        String currentShell = settings.getDefaultShell();
        for (int i = 0; i < shells.length; i++) {
            if (shells[i].equals(currentShell)) {
                binding.spinnerShell.setSelection(i);
                break;
            }
        }

        switch (settings.getFontSize()) {
            case AppSettings.FONT_SMALL:  binding.rgFontSize.check(binding.rbFontSmall.getId()); break;
            case AppSettings.FONT_LARGE:  binding.rgFontSize.check(binding.rbFontLarge.getId()); break;
            default:                      binding.rgFontSize.check(binding.rbFontMedium.getId()); break;
        }

        binding.switchTheme.setChecked(settings.isLightTheme());
        binding.switchShowExit.setChecked(settings.isShowExitCode());
        binding.switchFilterStderr.setChecked(settings.isFilterStderr());

        updateFontPreview();
        updateThemeLabel();
    }

    private void setupListeners() {
        binding.btnSave.setOnClickListener(v -> saveSettings());

        binding.btnResetPath.setOnClickListener(v -> {
            binding.inputTermuxPath.setText(AppSettings.DEFAULT_TERMUX_PATH);
        });

        binding.btnReset.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Reset Semua Pengaturan?")
                .setMessage("Semua pengaturan akan dikembalikan ke default.")
                .setPositiveButton("Reset", (d, w) -> {
                    settings.resetAll();
                    loadCurrentSettings();
                    Toast.makeText(this, "Pengaturan direset", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
        });

        binding.rgFontSize.setOnCheckedChangeListener((group, checkedId) -> updateFontPreview());

        binding.switchTheme.setOnCheckedChangeListener((btn, isChecked) -> updateThemeLabel());
    }

    private void saveSettings() {
        String path = binding.inputTermuxPath.getText().toString().trim();
        if (path.isEmpty()) {
            binding.inputTermuxPath.setError("Path tidak boleh kosong");
            return;
        }
        settings.setTermuxPath(path);

        String shell = binding.spinnerShell.getSelectedItem().toString();
        settings.setDefaultShell(shell);

        int checkedId = binding.rgFontSize.getCheckedRadioButtonId();
        if (checkedId == binding.rbFontSmall.getId())       settings.setFontSize(AppSettings.FONT_SMALL);
        else if (checkedId == binding.rbFontLarge.getId())  settings.setFontSize(AppSettings.FONT_LARGE);
        else                                                 settings.setFontSize(AppSettings.FONT_MEDIUM);

        settings.setTheme(binding.switchTheme.isChecked() ? AppSettings.THEME_LIGHT : AppSettings.THEME_DARK);
        settings.setShowExitCode(binding.switchShowExit.isChecked());
        settings.setFilterStderr(binding.switchFilterStderr.isChecked());

        Toast.makeText(this, "✅ Pengaturan disimpan!", Toast.LENGTH_SHORT).show();

        if (binding.switchTheme.isChecked() != settings.isLightTheme()) {
            recreate();
        }
    }

    private void updateFontPreview() {
        float sp;
        int checkedId = binding.rgFontSize.getCheckedRadioButtonId();
        if (checkedId == binding.rbFontSmall.getId())       sp = 11f;
        else if (checkedId == binding.rbFontLarge.getId())  sp = 15f;
        else                                                sp = 13f;

        binding.fontPreview.setTextSize(sp);
        binding.fontPreview.setText("Preview: $ pkg install wget\nFetching package list... Done\n✓ selesai (3s)");
    }

    private void updateThemeLabel() {
        binding.switchTheme.setText(binding.switchTheme.isChecked() ? "Mode Terang (Light)" : "Mode Gelap (Dark)");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
