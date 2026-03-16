package com.example.ezya;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import androidx.appcompat.app.AppCompatDelegate;
import java.util.Locale;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences prefs = getSharedPreferences("ezya_settings", MODE_PRIVATE);

        boolean isDark = prefs.getBoolean(SettingsActivity.KEY_THEME, true);
        AppCompatDelegate.setDefaultNightMode(isDark
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);

        String lang = prefs.getString(SettingsActivity.KEY_LANG, "ru");
        applyLocale(lang);
    }

    public void applyLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration(getResources().getConfiguration());
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}