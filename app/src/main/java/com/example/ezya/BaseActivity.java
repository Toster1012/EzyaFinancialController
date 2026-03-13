package com.example.ezya;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context base) {
        SharedPreferences prefs = base.getSharedPreferences("ezya_settings", MODE_PRIVATE);
        String lang = prefs.getString(SettingsActivity.KEY_LANG, "ru");
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        android.content.Context context = base.createConfigurationContext(config);
        super.attachBaseContext(context);
    }
}