package com.example.ezya;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.animation.OvershootInterpolator;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.ezya.databinding.ActivityDashboardBinding;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;

public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private TransactionAdapter transactionAdapter;
    private String currentPeriod;
    private double totalBudget;
    private List<Category> categoryList = new ArrayList<>();

    private static final int[] CHART_COLORS = {
            0xFFFFDD2D, 0xFFFF6B6B, 0xFF4ECDC4, 0xFF45B7D1,
            0xFF96CEB4, 0xFFFF8B94, 0xFFA8E6CF, 0xFFFFD3B6
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentPeriod = getIntent().getStringExtra("period");
        totalBudget = getIntent().getDoubleExtra("budget", 0);

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
                daysRemaining = now.getActualMaximum(Calendar.DAY_OF_MONTH) - now.get(Calendar.DAY_OF_MONTH);
                break;
            case "Год":
                daysRemaining = now.getActualMaximum(Calendar.DAY_OF_YEAR) - now.get(Calendar.DAY_OF_YEAR);
                break;
            default:
                daysRemaining = 0;
        }

        binding.daysRemainingTextView.setText(daysRemaining + " дн.");
    }

    private void setupTransactionList() {
        transactionAdapter = new TransactionAdapter();
        binding.transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.transactionsRecyclerView.setAdapter(transactionAdapter);
        binding.transactionsRecyclerView.setNestedScrollingEnabled(false);
    }

    private void observeData() {
        AppDatabase.getInstance(this)
                .categoryDao()
                .getCategoriesByPeriod(currentPeriod)
                .observe(this, categories -> {
                    categoryList = categories;
                    buildPieChart(categories);
                });

        AppDatabase.getInstance(this)
                .transactionDao()
                .getTransactionsByPeriod(currentPeriod)
                .observe(this, transactions ->
                        transactionAdapter.setTransactionList(transactions));
    }

    private void buildPieChart(List<Category> categories) {
        binding.pieChartContainer.removeAllViews();

        if (categories.isEmpty()) return;

        double total = 0;
        for (Category c : categories) total += c.getAmount();

        float startAngle = -90f;

        for (int i = 0; i < categories.size(); i++) {
            Category category = categories.get(i);
            float sweepAngle = (float) (category.getAmount() / total * 360f);
            int color = CHART_COLORS[i % CHART_COLORS.length];

            PieSliceView slice = new PieSliceView(this);
            slice.setSliceData(startAngle, sweepAngle, color,
                    category.getEmoji(), category.getName(),
                    category.getAmount());

            final int index = i;
            slice.setOnClickListener(v -> animateSlice(slice, index));

            binding.pieChartContainer.addView(slice);
            startAngle += sweepAngle;
        }
    }

    private void animateSlice(PieSliceView slice, int index) {
        binding.pieChartContainer.getChildAt(index)
                .animate()
                .scaleX(1.08f)
                .scaleY(1.08f)
                .setDuration(200)
                .setInterpolator(new OvershootInterpolator())
                .withEndAction(() ->
                        binding.pieChartContainer.getChildAt(index)
                                .animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(150)
                                .start())
                .start();

        binding.selectedCategoryTextView.setText(
                String.format("%s %s: %.0f ₽",
                        slice.getEmoji(),
                        slice.getCategoryName(),
                        slice.getCategoryAmount()));
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