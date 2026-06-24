package com.kztutorial.termuxiwx.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AppSettings {

    private static final String PREF_NAME = "termuxiwx_prefs";

    public static final String KEY_TERMUX_PATH    = "termux_path";
    public static final String KEY_DEFAULT_SHELL  = "default_shell";
    public static final String KEY_FONT_SIZE      = "font_size";
    public static final String KEY_THEME          = "app_theme";
    public static final String KEY_SHOW_EXIT_CODE = "show_exit_code";
    public static final String KEY_FILTER_STDERR  = "filter_stderr";

    public static final String DEFAULT_TERMUX_PATH = "/data/data/com.termux/files/usr/bin";
    public static final String DEFAULT_SHELL       = "bash";
    public static final String FONT_SMALL          = "small";
    public static final String FONT_MEDIUM         = "medium";
    public static final String FONT_LARGE          = "large";
    public static final String THEME_DARK          = "dark";
    public static final String THEME_LIGHT         = "light";

    public static final float FONT_SIZE_SMALL_SP  = 11f;
    public static final float FONT_SIZE_MEDIUM_SP = 13f;
    public static final float FONT_SIZE_LARGE_SP  = 15f;

    private final SharedPreferences prefs;

    public AppSettings(Context ctx) {
        prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public String getTermuxPath() {
        return prefs.getString(KEY_TERMUX_PATH, DEFAULT_TERMUX_PATH);
    }

    public void setTermuxPath(String path) {
        prefs.edit().putString(KEY_TERMUX_PATH, path).apply();
    }

    public String getDefaultShell() {
        return prefs.getString(KEY_DEFAULT_SHELL, DEFAULT_SHELL);
    }

    public void setDefaultShell(String shell) {
        prefs.edit().putString(KEY_DEFAULT_SHELL, shell).apply();
    }

    public String getFontSize() {
        return prefs.getString(KEY_FONT_SIZE, FONT_MEDIUM);
    }

    public void setFontSize(String size) {
        prefs.edit().putString(KEY_FONT_SIZE, size).apply();
    }

    public float getFontSizeSp() {
        switch (getFontSize()) {
            case FONT_SMALL: return FONT_SIZE_SMALL_SP;
            case FONT_LARGE: return FONT_SIZE_LARGE_SP;
            default:         return FONT_SIZE_MEDIUM_SP;
        }
    }

    public String getTheme() {
        return prefs.getString(KEY_THEME, THEME_DARK);
    }

    public void setTheme(String theme) {
        prefs.edit().putString(KEY_THEME, theme).apply();
    }

    public boolean isLightTheme() {
        return THEME_LIGHT.equals(getTheme());
    }

    public boolean isShowExitCode() {
        return prefs.getBoolean(KEY_SHOW_EXIT_CODE, true);
    }

    public void setShowExitCode(boolean show) {
        prefs.edit().putBoolean(KEY_SHOW_EXIT_CODE, show).apply();
    }

    public boolean isFilterStderr() {
        return prefs.getBoolean(KEY_FILTER_STDERR, true);
    }

    public void setFilterStderr(boolean filter) {
        prefs.edit().putBoolean(KEY_FILTER_STDERR, filter).apply();
    }

    public void resetAll() {
        prefs.edit().clear().apply();
    }
}
