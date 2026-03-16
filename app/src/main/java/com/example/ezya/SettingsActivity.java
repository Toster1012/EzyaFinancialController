package com.example.ezya;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.ezya.databinding.ActivitySettingsBinding;
import java.util.Locale;
import java.util.concurrent.Executors;

public class SettingsActivity extends BaseActivity {

    static final String PREFS_SETTINGS = "ezya_settings";
    static final String KEY_THEME = "theme_dark";
    static final String KEY_LANG = "language";

    private ActivitySettingsBinding binding;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = getSharedPreferences(PREFS_SETTINGS, MODE_PRIVATE);
        boolean isDark = prefs.getBoolean(KEY_THEME, true);
        String lang = prefs.getString(KEY_LANG, "ru");

        binding.themeSwitch.setChecked(isDark);
        updateThemeLabel(isDark);
        updateLangButtons(lang);

        binding.backButton.setOnClickListener(v -> {
            SoundManager.getInstance(this).playTap();
            finish();
        });

        binding.themeSwitch.setOnCheckedChangeListener((btn, isChecked) -> {
            SoundManager.getInstance(this).playTap();
            prefs.edit().putBoolean(KEY_THEME, isChecked).apply();
            updateThemeLabel(isChecked);
            AppCompatDelegate.setDefaultNightMode(isChecked
                    ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_NO);
            recreate();
        });

        binding.langRuButton.setOnClickListener(v -> {
            SoundManager.getInstance(this).playTap();
            setLocale("ru");
        });

        binding.langEngButton.setOnClickListener(v -> {
            SoundManager.getInstance(this).playTap();
            setLocale("en");
        });

        binding.clearHistoryButton.setOnClickListener(v -> {
            SoundManager.getInstance(this).playTap();
            confirmClearHistory();
        });

        binding.rateAppButton.setOnClickListener(v -> {
            SoundManager.getInstance(this).playTap();
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + getPackageName())));
            } catch (Exception e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
            }
        });

        binding.reportBugButton.setOnClickListener(v -> {
            SoundManager.getInstance(this).playTap();
            Intent email = new Intent(Intent.ACTION_SENDTO);
            email.setData(Uri.parse("mailto:support@ezya.app"));
            email.putExtra(Intent.EXTRA_SUBJECT, "Bug report - Finance Calculator");
            try { startActivity(email); } catch (Exception ignored) {}
        });
    }

    private void updateThemeLabel(boolean isDark) {
        binding.themeSwitch.setText(isDark
                ? getString(R.string.theme_dark)
                : getString(R.string.theme_light));
    }

    private void updateLangButtons(String lang) {
        if (lang.equals("ru")) {
            binding.langRuButton.setTextColor(0xFFFFDD2D);
            binding.langEngButton.setTextColor(0xFF8A8A8A);
        } else {
            binding.langEngButton.setTextColor(0xFFFFDD2D);
            binding.langRuButton.setTextColor(0xFF8A8A8A);
        }
    }

    private void setLocale(String lang) {
        prefs.edit().putString(KEY_LANG, lang).apply();
        ((App) getApplication()).applyLocale(lang);
        updateLangButtons(lang);
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
            runOnUiThread(() -> {
                SoundManager.getInstance(this).playDelete();
                Toast.makeText(this, R.string.history_cleared, Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
