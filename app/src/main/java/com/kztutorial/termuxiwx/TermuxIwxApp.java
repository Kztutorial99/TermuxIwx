package com.kztutorial.termuxiwx;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.kztutorial.termuxiwx.utils.AppSettings;

public class TermuxIwxApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppSettings settings = new AppSettings(this);
        if (settings.isLightTheme()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }
}
