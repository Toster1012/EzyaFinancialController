package com.example.ezya;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.ezya.databinding.ActivityDashboardBinding;
import java.util.concurrent.Executors;

public class DashboardActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "ezya_prefs";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_PERIOD = "selected_period";

    private ActivityDashboardBinding binding;
    private TransactionAdapter transactionAdapter;
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
        setupTransactionList();
        observeData();

        binding.closeButton.setOnClickListener(v -> confirmDeleteAndExit());
        binding.addTransactionButton.setOnClickListener(v ->
                AddTransactionBottomSheet.newInstance(currentPeriod)
                        .show(getSupportFragmentManager(), "AddTransaction"));
    }

    private void setupDaysRemaining() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long startTime = prefs.getLong(KEY_START_TIME, System.currentTimeMillis());
        long now = System.currentTimeMillis();
        long elapsedMs = now - startTime;

        long periodMs;
        switch (currentPeriod) {
            case "Неделя":
                periodMs = 7L * 24 * 60 * 60 * 1000;
                break;
            case "Месяц":
                periodMs = 30L * 24 * 60 * 60 * 1000;
                break;
            case "Год":
                periodMs = 365L * 24 * 60 * 60 * 1000;
                break;
            default:
                periodMs = 7L * 24 * 60 * 60 * 1000;
        }

        long remainingMs = periodMs - elapsedMs;
        long daysRemaining = remainingMs > 0 ? remainingMs / (24 * 60 * 60 * 1000) : 0;

        binding.daysRemainingTextView.setText(daysRemaining + " дн. осталось");
    }

    private void setupTransactionList() {
        transactionAdapter = new TransactionAdapter();
        binding.transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.transactionsRecyclerView.setAdapter(transactionAdapter);
        binding.transactionsRecyclerView.setNestedScrollingEnabled(false);
    }

    private void observeData() {
        AppDatabase db = AppDatabase.getInstance(this);

        db.transactionDao().getIncomeSummaryByPeriod(currentPeriod)
                .observe(this, summaries -> {
                    binding.incomeChartView.setSummaries(summaries);
                    double total = 0;
                    for (CategorySummary s : summaries) total += s.getAmount();
                    binding.totalIncomeTextView.setText(String.format("Доходы: %.0f ₽", total));
                });

        db.transactionDao().getExpenseSummaryByPeriod(currentPeriod)
                .observe(this, summaries -> {
                    binding.expenseChartView.setSummaries(summaries);
                    double total = 0;
                    for (CategorySummary s : summaries) total += s.getAmount();
                    binding.totalExpenseTextView.setText(String.format("Расходы: %.0f ₽", total));
                });

        db.transactionDao().getTransactionsByPeriod(currentPeriod)
                .observe(this, transactions ->
                        transactionAdapter.setTransactionList(transactions));

        binding.incomeChartView.setSliceClickListener((emoji, name, amount) ->
                binding.selectedCategoryTextView.setText(
                        String.format("%s %s: %.0f ₽", emoji, name, amount)));

        binding.expenseChartView.setSliceClickListener((emoji, name, amount) ->
                binding.selectedCategoryTextView.setText(
                        String.format("%s %s: %.0f ₽", emoji, name, amount)));
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