package com.kztutorial.termuxiwx.ui;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.kztutorial.termuxiwx.databinding.ActivityScriptRepoBinding;
import com.kztutorial.termuxiwx.models.ScriptItem;
import com.kztutorial.termuxiwx.ui.adapters.ScriptAdapter;
import com.kztutorial.termuxiwx.utils.TermuxConnector;

import java.util.ArrayList;
import java.util.List;

public class ScriptRepoActivity extends AppCompatActivity {

    private static final String ACTION_RESULT = "com.kztutorial.termuxiwx.SCRIPT_RESULT";
    private ActivityScriptRepoBinding binding;
    private ScriptAdapter adapter;
    private final List<ScriptItem> allScripts = new ArrayList<>();
    private final List<ScriptItem> filteredScripts = new ArrayList<>();
    private ScriptItem pendingInstall = null;

    private final BroadcastReceiver resultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            Bundle extras = intent.getExtras();
            Bundle result = extras != null ? extras.getBundle("result") : null;
            int exitCode = result != null ? result.getInt("exitCode", -1) : -1;
            runOnUiThread(() -> {
                String name = pendingInstall != null ? pendingInstall.getName() : "Tool";
                if (exitCode == 0) {
                    Snackbar.make(binding.getRoot(), "✅ " + name + " berhasil diinstall!", Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(binding.getRoot(), "❌ Gagal install " + name + ". Cek Console.", Snackbar.LENGTH_LONG).show();
                }
                pendingInstall = null;
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScriptRepoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Script & Tools Repository");
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        setupScripts();
        setupRecyclerView();
        setupSearch();
        setupCategoryChips();
    }

    private void setupScripts() {
        allScripts.add(new ScriptItem("wget", "Downloader via HTTP/FTP/HTTPS", "pkg install -y wget", "Tool", "wget --version"));
        allScripts.add(new ScriptItem("curl", "HTTP client & data transfer", "pkg install -y curl", "Tool", "curl --version"));
        allScripts.add(new ScriptItem("git", "Version control system", "pkg install -y git", "Tool", "git --version"));
        allScripts.add(new ScriptItem("python", "Python 3 interpreter", "pkg install -y python", "Dev", "python --version"));
        allScripts.add(new ScriptItem("nodejs", "JavaScript runtime (Node.js)", "pkg install -y nodejs", "Dev", "node --version"));
        allScripts.add(new ScriptItem("php", "PHP interpreter", "pkg install -y php", "Dev", "php --version"));
        allScripts.add(new ScriptItem("ruby", "Ruby interpreter", "pkg install -y ruby", "Dev", "ruby --version"));
        allScripts.add(new ScriptItem("golang", "Go programming language", "pkg install -y golang", "Dev", "go version"));
        allScripts.add(new ScriptItem("rust", "Rust compiler", "pkg install -y rust", "Dev", "rustc --version"));
        allScripts.add(new ScriptItem("clang", "C/C++ compiler", "pkg install -y clang", "Dev", "clang --version"));
        allScripts.add(new ScriptItem("openjdk-17", "Java Development Kit 17", "pkg install -y openjdk-17", "Dev", "java --version"));
        allScripts.add(new ScriptItem("pip packages", "Install common Python libs", "pip install requests beautifulsoup4 flask django numpy pandas", "Dev", "pip list"));
        allScripts.add(new ScriptItem("nmap", "Network scanner & security", "pkg install -y nmap", "Security", "nmap --version"));
        allScripts.add(new ScriptItem("hydra", "Password brute-force tool", "pkg install -y hydra", "Security", "hydra -h 2>&1 | head -5"));
        allScripts.add(new ScriptItem("metasploit", "Penetration testing framework", "pkg install -y unstable-repo && pkg install -y metasploit", "Security", "msfconsole --version"));
        allScripts.add(new ScriptItem("sqlmap", "SQL injection scanner", "pkg install -y sqlmap", "Security", "sqlmap --version"));
        allScripts.add(new ScriptItem("aircrack-ng", "WiFi security testing", "pkg install -y aircrack-ng", "Security", "aircrack-ng --version"));
        allScripts.add(new ScriptItem("openssh", "SSH client & server", "pkg install -y openssh", "Network", "ssh -V"));
        allScripts.add(new ScriptItem("net-tools", "ifconfig, netstat, route", "pkg install -y net-tools", "Network", "ifconfig --version"));
        allScripts.add(new ScriptItem("iproute2", "IP routing tools (ip cmd)", "pkg install -y iproute2", "Network", "ip --version"));
        allScripts.add(new ScriptItem("tmate", "Terminal sharing via SSH", "pkg install -y tmate", "Network", "tmate -V"));
        allScripts.add(new ScriptItem("termux-api", "Termux Android API access", "pkg install -y termux-api", "Termux", "termux-info"));
        allScripts.add(new ScriptItem("termux-setup-storage", "Enable storage access", "termux-setup-storage", "Termux", "ls ~/storage"));
        allScripts.add(new ScriptItem("ffmpeg", "Media converter & processor", "pkg install -y ffmpeg", "Media", "ffmpeg -version"));
        allScripts.add(new ScriptItem("imagemagick", "Image manipulation tool", "pkg install -y imagemagick", "Media", "convert --version"));
        allScripts.add(new ScriptItem("vim", "Advanced text editor", "pkg install -y vim", "Editor", "vim --version | head -1"));
        allScripts.add(new ScriptItem("neovim", "Modern vim text editor", "pkg install -y neovim", "Editor", "nvim --version | head -1"));
        allScripts.add(new ScriptItem("tmux", "Terminal multiplexer", "pkg install -y tmux", "Tool", "tmux -V"));
        allScripts.add(new ScriptItem("htop", "Interactive process viewer", "pkg install -y htop", "Tool", "htop --version"));
        allScripts.add(new ScriptItem("neofetch", "System info display", "pkg install -y neofetch", "Tool", "neofetch"));
        allScripts.add(new ScriptItem("zsh", "Z shell (alternative bash)", "pkg install -y zsh && chsh -s zsh", "Shell", "zsh --version"));
        allScripts.add(new ScriptItem("fish", "Friendly interactive shell", "pkg install -y fish", "Shell", "fish --version"));
        allScripts.add(new ScriptItem("oh-my-zsh", "Zsh framework & themes", "pkg install -y zsh && sh -c \"$(curl -fsSL https://raw.github.com/ohmyzsh/ohmyzsh/master/tools/install.sh)\"", "Shell", "zsh --version"));
        allScripts.add(new ScriptItem("mariadb", "MySQL-compatible database", "pkg install -y mariadb", "Database", "mysql --version"));
        allScripts.add(new ScriptItem("postgresql", "PostgreSQL database", "pkg install -y postgresql", "Database", "psql --version"));
        allScripts.add(new ScriptItem("redis", "In-memory data store", "pkg install -y redis", "Database", "redis-server --version"));
        filteredScripts.addAll(allScripts);
    }

    private void setupRecyclerView() {
        adapter = new ScriptAdapter(filteredScripts, this::showInstallDialog);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        binding.countText.setText(filteredScripts.size() + " tools");
    }

    private void showInstallDialog(ScriptItem item) {
        new AlertDialog.Builder(this)
            .setTitle("📦 " + item.getName())
            .setMessage(item.getDescription() + "\n\nPerintah install:\n" + item.getInstallCmd())
            .setPositiveButton("⚡ Install", (d, w) -> {
                pendingInstall = item;
                TermuxConnector.customCommand(this, item.getInstallCmd(), buildPendingIntent());
                Snackbar.make(binding.getRoot(), "Menginstall " + item.getName() + "...", Snackbar.LENGTH_SHORT).show();
            })
            .setNeutralButton("🖥 Buka Console", (d, w) -> {
                Intent console = new Intent(this, ConsoleActivity.class);
                console.putExtra("initial_cmd", item.getInstallCmd());
                startActivity(console);
            })
            .setNegativeButton("Batal", null)
            .show();
    }

    private void setupSearch() {
        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int i, int b, int c) {}
            @Override
            public void afterTextChanged(Editable s) {
                filterScripts(s.toString(), getCurrentCategory());
            }
        });
    }

    private void setupCategoryChips() {
        binding.chipAll.setOnClickListener(v -> filterScripts(getQuery(), "All"));
        binding.chipDev.setOnClickListener(v -> filterScripts(getQuery(), "Dev"));
        binding.chipSecurity.setOnClickListener(v -> filterScripts(getQuery(), "Security"));
        binding.chipNetwork.setOnClickListener(v -> filterScripts(getQuery(), "Network"));
        binding.chipTool.setOnClickListener(v -> filterScripts(getQuery(), "Tool"));
        binding.chipShell.setOnClickListener(v -> filterScripts(getQuery(), "Shell"));
        binding.chipTermux.setOnClickListener(v -> filterScripts(getQuery(), "Termux"));
        binding.chipDatabase.setOnClickListener(v -> filterScripts(getQuery(), "Database"));
        binding.chipEditor.setOnClickListener(v -> filterScripts(getQuery(), "Editor"));
        binding.chipMedia.setOnClickListener(v -> filterScripts(getQuery(), "Media"));
    }

    private String getQuery() {
        return binding.searchInput.getText().toString().toLowerCase().trim();
    }

    private String getCurrentCategory() {
        if (binding.chipDev.isChecked()) return "Dev";
        if (binding.chipSecurity.isChecked()) return "Security";
        if (binding.chipNetwork.isChecked()) return "Network";
        if (binding.chipTool.isChecked()) return "Tool";
        if (binding.chipShell.isChecked()) return "Shell";
        if (binding.chipTermux.isChecked()) return "Termux";
        if (binding.chipDatabase.isChecked()) return "Database";
        if (binding.chipEditor.isChecked()) return "Editor";
        if (binding.chipMedia.isChecked()) return "Media";
        return "All";
    }

    private void filterScripts(String query, String category) {
        List<ScriptItem> newList = new ArrayList<>();
        for (ScriptItem item : allScripts) {
            boolean matchQuery = query.isEmpty()
                || item.getName().toLowerCase().contains(query)
                || item.getDescription().toLowerCase().contains(query);
            boolean matchCat = category.equals("All") || item.getCategory().equals(category);
            if (matchQuery && matchCat) newList.add(item);
        }
        adapter.updateList(newList);
        binding.countText.setText(newList.size() + " tools");
    }

    private PendingIntent buildPendingIntent() {
        Intent intent = new Intent(ACTION_RESULT);
        intent.setPackage(getPackageName());
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) flags |= PendingIntent.FLAG_MUTABLE;
        return PendingIntent.getBroadcast(this, 3, intent, flags);
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
        finish();
        return true;
    }
}
