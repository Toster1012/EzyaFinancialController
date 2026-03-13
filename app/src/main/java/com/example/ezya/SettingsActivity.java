package com.example.ezya;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.ezya.databinding.ActivitySettingsBinding;
import java.util.Locale;
import java.util.concurrent.Executors;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_SETTINGS = "ezya_settings";
    static final String KEY_THEME = "theme_dark";
    static final String KEY_LANG = "language";

    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences prefs = getSharedPreferences(PREFS_SETTINGS, MODE_PRIVATE);
        boolean isDark = prefs.getBoolean(KEY_THEME, true);
        String lang = prefs.getString(KEY_LANG, "ru");

        binding.themeSwitch.setChecked(isDark);
        binding.themeSwitch.setText(isDark ? R.string.theme_dark : R.string.theme_light);

        if (lang.equals("ru")) {
            binding.langRuButton.setSelected(true);
            binding.langRuButton.setTextColor(0xFFFFDD2D);
            binding.langEngButton.setTextColor(0xFF8A8A8A);
        } else {
            binding.langEngButton.setSelected(true);
            binding.langEngButton.setTextColor(0xFFFFDD2D);
            binding.langRuButton.setTextColor(0xFF8A8A8A);
        }

        binding.backButton.setOnClickListener(v -> finish());

        binding.themeSwitch.setOnCheckedChangeListener((btn, isChecked) -> {
            binding.themeSwitch.setText(isChecked ? R.string.theme_dark : R.string.theme_light);
            prefs.edit().putBoolean(KEY_THEME, isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(isChecked
                    ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_NO);
        });

        binding.langRuButton.setOnClickListener(v -> {
            setLocale("ru", prefs);
            binding.langRuButton.setTextColor(0xFFFFDD2D);
            binding.langEngButton.setTextColor(0xFF8A8A8A);
        });

        binding.langEngButton.setOnClickListener(v -> {
            setLocale("en", prefs);
            binding.langEngButton.setTextColor(0xFFFFDD2D);
            binding.langRuButton.setTextColor(0xFF8A8A8A);
        });

        binding.clearHistoryButton.setOnClickListener(v -> confirmClearHistory());
    }

    private void setLocale(String lang, SharedPreferences prefs) {
        prefs.edit().putString(KEY_LANG, lang).apply();
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        recreate();
    }

    private void confirmClearHistory() {
        new androidx.appcompat.app.AlertDialog.Builder(this, R.style.DarkAlertDialog)
                .setTitle(R.string.clear_history_title)
                .setMessage(R.string.clear_history_message)
                .setPositiveButton(R.string.yes, (d, w) -> clearHistory())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void clearHistory() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            db.periodRecordDao().deleteAll();
            db.archivedTransactionDao().deleteAll();
            runOnUiThread(() ->
                    Toast.makeText(this, R.string.history_cleared, Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}