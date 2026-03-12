package com.example.ezya;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.ezya.databinding.ActivityDashboardBinding;
import java.util.Calendar;
import java.util.concurrent.Executors;

public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private TransactionAdapter transactionAdapter;
    private String currentPeriod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentPeriod = getIntent().getStringExtra("period");

        setupDaysRemaining();
        setupTransactionList();
        observeData();

        binding.closeButton.setOnClickListener(v -> confirmDeleteAndExit());
        binding.addTransactionButton.setOnClickListener(v ->
                AddTransactionBottomSheet.newInstance(currentPeriod)
                        .show(getSupportFragmentManager(), "AddTransaction"));
    }

    private void setupDaysRemaining() {
        Calendar now = Calendar.getInstance();
        int daysRemaining;
        switch (currentPeriod) {
            case "Неделя":
                daysRemaining = 7 - now.get(Calendar.DAY_OF_WEEK) + 1;
                break;
            case "Месяц":
                daysRemaining = now.getActualMaximum(Calendar.DAY_OF_MONTH)
                        - now.get(Calendar.DAY_OF_MONTH);
                break;
            case "Год":
                daysRemaining = now.getActualMaximum(Calendar.DAY_OF_YEAR)
                        - now.get(Calendar.DAY_OF_YEAR);
                break;
            default:
                daysRemaining = 0;
        }
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

        db.categoryDao().getCategoriesByPeriodAndType(currentPeriod, true)
                .observe(this, categories -> {
                    binding.incomeChartView.setCategories(categories);
                    double total = 0;
                    for (Category c : categories) total += c.getAmount();
                    binding.totalIncomeTextView.setText(String.format("Доходы: %.0f ₽", total));
                });

        db.categoryDao().getCategoriesByPeriodAndType(currentPeriod, false)
                .observe(this, categories -> {
                    binding.expenseChartView.setCategories(categories);
                    double total = 0;
                    for (Category c : categories) total += c.getAmount();
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