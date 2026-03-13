package com.example.ezya;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ezya.databinding.ActivityDashboardBinding;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.concurrent.Executors;

public class DashboardActivity extends AppCompatActivity {

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

        binding.closeButton.setOnClickListener(v -> confirmDeleteAndExit());
        binding.addTransactionButton.setOnClickListener(v -> openAddTransaction());
        binding.endPeriodTestButton.setOnClickListener(v -> openPeriodSummary());
    }

    private void setupViewPager() {
        DashboardPagerAdapter adapter = new DashboardPagerAdapter(this, currentPeriod);
        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(position == 0 ? "Доходы" : "Расходы")
        ).attach();
    }

    private void openAddTransaction() {
        int currentTab = binding.viewPager.getCurrentItem();
        boolean isExpense = currentTab == 1;
        AddTransactionBottomSheet.newInstance(currentPeriod, isExpense)
                .show(getSupportFragmentManager(), "AddTransaction");
    }

    private void setupDaysRemaining() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long startTime = prefs.getLong(KEY_START_TIME, System.currentTimeMillis());
        long now = System.currentTimeMillis();
        long elapsedMs = now - startTime;

        long periodMs;
        switch (currentPeriod) {
            case "Неделя": periodMs = 7L * 24 * 60 * 60 * 1000; break;
            case "Месяц": periodMs = 30L * 24 * 60 * 60 * 1000; break;
            case "Год": periodMs = 365L * 24 * 60 * 60 * 1000; break;
            default: periodMs = 7L * 24 * 60 * 60 * 1000;
        }

        long remainingMs = periodMs - elapsedMs;
        long daysRemaining = remainingMs > 0 ? remainingMs / (24 * 60 * 60 * 1000) : 0;
        binding.daysRemainingTextView.setText(daysRemaining + " дн. осталось");
    }

    private void checkPeriodExpired() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long startTime = prefs.getLong(KEY_START_TIME, System.currentTimeMillis());
        long elapsedMs = System.currentTimeMillis() - startTime;

        long periodMs;
        switch (currentPeriod) {
            case "Неделя": periodMs = 7L * 24 * 60 * 60 * 1000; break;
            case "Месяц": periodMs = 30L * 24 * 60 * 60 * 1000; break;
            case "Год": periodMs = 365L * 24 * 60 * 60 * 1000; break;
            default: periodMs = 7L * 24 * 60 * 60 * 1000;
        }

        if (elapsedMs >= periodMs) openPeriodSummary();
    }

    private void openPeriodSummary() {
        Intent intent = new Intent(this, PeriodSummaryActivity.class);
        intent.putExtra("period", currentPeriod);
        startActivity(intent);
    }

    private void confirmDeleteAndExit() {
        new AlertDialog.Builder(this, R.style.DarkAlertDialog)
                .setTitle("Удалить данные?")
                .setMessage("Все категории и транзакции будут удалены.")
                .setPositiveButton("Да", (dialog, which) -> deleteAllAndExit())
                .setNegativeButton("Нет", null)
                .show();
    }

    private void deleteAllAndExit() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            db.categoryDao().deleteAll();
            db.transactionDao().deleteAll();
            runOnUiThread(() -> {
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().clear().apply();
                Intent intent = new Intent(this, WelcomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
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