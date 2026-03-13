package com.example.ezya;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class WelcomeActivity extends BaseActivity {

    private static final String PREFS_NAME = "ezya_prefs";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_PERIOD = "selected_period";
    private static final String PREFS_SETTINGS = "ezya_settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long startTime = prefs.getLong(KEY_START_TIME, 0);
        double totalIncome = prefs.getFloat("total_income", 0f);

        if (startTime > 0 && totalIncome > 0) {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("period", prefs.getString(KEY_PERIOD, "Неделя"));
            startActivity(intent);
            finish();
            return;
        }

        findViewById(R.id.startButton).setOnClickListener(v ->
                startActivity(new Intent(this, BudgetActivity.class)));
    }

    private void applyTheme() {
        SharedPreferences prefs = getSharedPreferences(PREFS_SETTINGS, MODE_PRIVATE);
        boolean isDark = prefs.getBoolean(SettingsActivity.KEY_THEME, true);
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(isDark
                ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
    }
}