package com.kztutorial.termuxiwx.utils;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

public class TermuxConnector {

    public static final String TERMUX_PACKAGE = "com.termux";
    public static final String RUN_COMMAND_SERVICE = "com.termux.app.RunCommandService";
    public static final String ACTION_RUN_COMMAND = "com.termux.RUN_COMMAND";

    public static final String EXTRA_COMMAND_PATH = "com.termux.RUN_COMMAND_PATH";
    public static final String EXTRA_ARGUMENTS = "com.termux.RUN_COMMAND_ARGUMENTS";
    public static final String EXTRA_WORKDIR = "com.termux.RUN_COMMAND_WORKDIR";
    public static final String EXTRA_BACKGROUND = "com.termux.RUN_COMMAND_BACKGROUND";
    public static final String EXTRA_STDIN = "com.termux.RUN_COMMAND_STDIN";
    public static final String EXTRA_PENDING_INTENT = "com.termux.RUN_COMMAND_PENDING_INTENT";
    public static final String EXTRA_COMMAND_LABEL = "com.termux.RUN_COMMAND_LABEL";

    private static String getBinPath(Context context) {
        return new AppSettings(context).getTermuxPath();
    }

    private static String getShell(Context context) {
        String shell = new AppSettings(context).getDefaultShell();
        return getBinPath(context) + "/" + shell;
    }

    public static boolean isTermuxInstalled(Context context) {
        try {
            context.getPackageManager().getPackageInfo(TERMUX_PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void runCommand(Context context, String command, String[] args,
                                   boolean background, PendingIntent resultIntent, String label) {
        Intent intent = new Intent(ACTION_RUN_COMMAND);
        intent.setComponent(new ComponentName(TERMUX_PACKAGE, RUN_COMMAND_SERVICE));
        intent.putExtra(EXTRA_COMMAND_PATH, command);
        intent.putExtra(EXTRA_WORKDIR, "/data/data/com.termux/files/home");
        intent.putExtra(EXTRA_BACKGROUND, background);
        intent.putExtra(EXTRA_COMMAND_LABEL, label != null ? label : "TermuxIwx");
        if (args != null) intent.putExtra(EXTRA_ARGUMENTS, args);
        if (resultIntent != null) intent.putExtra(EXTRA_PENDING_INTENT, resultIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void aptSearch(Context context, String query, PendingIntent resultIntent) {
        runCommand(context, getBinPath(context) + "/apt",
                new String[]{"search", query},
                true, resultIntent, "apt search " + query);
    }

    public static void aptInstall(Context context, String packageName, PendingIntent resultIntent) {
        runCommand(context, getBinPath(context) + "/apt",
                new String[]{"install", "-y", packageName},
                true, resultIntent, "apt install " + packageName);
    }

    public static void aptUpdate(Context context, PendingIntent resultIntent) {
        runCommand(context, getBinPath(context) + "/apt",
                new String[]{"update"},
                true, resultIntent, "apt update");
    }

    public static void aptUpgrade(Context context, PendingIntent resultIntent) {
        runCommand(context, getBinPath(context) + "/apt",
                new String[]{"upgrade", "-y"},
                true, resultIntent, "apt upgrade");
    }

    public static void aptListInstalled(Context context, PendingIntent resultIntent) {
        runCommand(context, getBinPath(context) + "/dpkg",
                new String[]{"-l"},
                true, resultIntent, "dpkg -l");
    }

    public static void aptRemove(Context context, String packageName, PendingIntent resultIntent) {
        runCommand(context, getBinPath(context) + "/apt",
                new String[]{"remove", "-y", packageName},
                true, resultIntent, "apt remove " + packageName);
    }

    public static void pkgInfo(Context context, String packageName, PendingIntent resultIntent) {
        runCommand(context, getBinPath(context) + "/apt",
                new String[]{"show", packageName},
                true, resultIntent, "apt show " + packageName);
    }

    public static void runScript(Context context, String scriptPath, PendingIntent resultIntent) {
        runCommand(context, getShell(context),
                new String[]{scriptPath},
                false, resultIntent, "run script");
    }

    public static void listScripts(Context context, PendingIntent resultIntent) {
        runCommand(context, getBinPath(context) + "/find",
                new String[]{"/data/data/com.termux/files/home", "-name", "*.sh", "-o", "-name", "*.py", "-o", "-name", "*.rb"},
                true, resultIntent, "list scripts");
    }

    public static void getStorageInfo(Context context, PendingIntent resultIntent) {
        runCommand(context, getBinPath(context) + "/df",
                new String[]{"-h"},
                true, resultIntent, "df -h");
    }

    public static void customCommand(Context context, String cmd, PendingIntent resultIntent) {
        runCommand(context, getShell(context),
                new String[]{"-c", cmd},
                true, resultIntent, "custom: " + cmd);
    }
}
