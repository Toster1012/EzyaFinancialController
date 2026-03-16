package com.example.ezya;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import com.example.ezya.databinding.ActivityDashboardBinding;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.concurrent.Executors;

public class DashboardActivity extends BaseActivity {

    private static final String PREFS_NAME = "ezya_prefs";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_PERIOD = "selected_period";

    private ActivityDashboardBinding binding;
    private String currentPeriod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentPeriod = getIntent().getStringExtra("period");
        if (currentPeriod == null) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            currentPeriod = prefs.getString(KEY_PERIOD, "Неделя");
        }

        setupDaysRemaining();
        setupViewPager();
        checkPeriodExpired();

        binding.closeButton.setOnClickListener(v -> {
            SoundManager.getInstance(this).playTap();
            confirmDeleteAndExit();
        });
        binding.addTransactionButton.setOnClickListener(v -> {
            SoundManager.getInstance(this).playTap();
            openAddTransaction();
        });
        binding.endPeriodTestButton.setOnClickListener(v -> {
            SoundManager.getInstance(this).playTap();
            openPeriodSummary();
        });
        binding.historyButton.setOnClickListener(v -> {
            SoundManager.getInstance(this).playTap();
            startActivity(new Intent(this, HistoryActivity.class));
        });
        binding.settingsButton.setOnClickListener(v -> {
            SoundManager.getInstance(this).playTap();
            startActivity(new Intent(this, SettingsActivity.class));
        });
    }

    private void setupViewPager() {
        DashboardPagerAdapter adapter = new DashboardPagerAdapter(this, currentPeriod);
        binding.viewPager.setAdapter(adapter);
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(position == 0
                        ? getString(R.string.income) : getString(R.string.expense))
        ).attach();
    }

    private void openAddTransaction() {
        boolean isExpense = binding.viewPager.getCurrentItem() == 1;
        AddTransactionBottomSheet.newInstance(currentPeriod, isExpense)
                .show(getSupportFragmentManager(), "AddTransaction");
    }

    private void setupDaysRemaining() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long startTime = prefs.getLong(KEY_START_TIME, System.currentTimeMillis());
        long remainingMs = getPeriodMs() - (System.currentTimeMillis() - startTime);
        long days = remainingMs > 0 ? remainingMs / (24 * 60 * 60 * 1000) : 0;
        binding.daysRemainingTextView.setText(getString(R.string.days_remaining, days));
    }

    private void checkPeriodExpired() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long startTime = prefs.getLong(KEY_START_TIME, System.currentTimeMillis());
        if (System.currentTimeMillis() - startTime >= getPeriodMs()) openPeriodSummary();
    }

    private long getPeriodMs() {
        switch (currentPeriod) {
            case "Месяц": case "Month": return 30L * 24 * 60 * 60 * 1000;
            case "Год": case "Year":   return 365L * 24 * 60 * 60 * 1000;
            default:                   return 7L * 24 * 60 * 60 * 1000;
        }
    }

    private void openPeriodSummary() {
        startActivity(new Intent(this, PeriodSummaryActivity.class)
                .putExtra("period", currentPeriod));
    }

    private void confirmDeleteAndExit() {
        new AlertDialog.Builder(this, R.style.DarkAlertDialog)
                .setTitle(R.string.delete_data_title)
                .setMessage(R.string.delete_data_message)
                .setPositiveButton(R.string.yes, (d, w) -> deleteAllAndExit())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void deleteAllAndExit() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            db.categoryDao().deleteAll();
            db.transactionDao().deleteAll();
            runOnUiThread(() -> {
                SoundManager.getInstance(this).playDelete();
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().clear().apply();
                Intent intent = new Intent(this, WelcomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}