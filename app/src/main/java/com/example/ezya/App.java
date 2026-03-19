package com.example.ezya;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.ezya.di.AppContainer;
import com.example.ezya.ui.settings.SettingsActivity;
import java.util.Locale;

public class App extends Application {

    public AppContainer container;

    @Override
    public void onCreate() {
        super.onCreate();
        container = new AppContainer(this);

        SharedPreferences prefs = getSharedPreferences("ezya_settings", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean(SettingsActivity.KEY_THEME, true);
        AppCompatDelegate.setDefaultNightMode(isDark
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
        applyLocale(prefs.getString(SettingsActivity.KEY_LANG, "ru"));
    }

    public void applyLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration(getResources().getConfiguration());
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    public static App from(android.content.Context context) {
        return (App) context.getApplicationContext();
    }
}