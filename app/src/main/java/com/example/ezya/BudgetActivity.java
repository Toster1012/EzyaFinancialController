package com.example.ezya;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.ezya.databinding.ActivityBudgetBinding;
import java.util.concurrent.Executors;

public class BudgetActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "ezya_prefs";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_PERIOD = "selected_period";

    private ActivityBudgetBinding binding;
    private CategoryAdapter categoryAdapter;
    private String selectedPeriod = "Неделя";
    private boolean isIncomeStep = true;
    private double totalIncome = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBudgetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupPeriodDropdown();
        setupRecyclerView();
        observeCategories();
        updateStepUi();

        binding.addCategoryButton.setOnClickListener(v -> openAddCategorySheet());
        binding.nextButton.setOnClickListener(v -> onNextClicked());
    }

    private void setupPeriodDropdown() {
        String[] periods = {"Неделя", "Месяц", "Год"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, periods);
        binding.periodAutoCompleteTextView.setAdapter(adapter);
        binding.periodAutoCompleteTextView.setText(selectedPeriod, false);
        binding.periodAutoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            selectedPeriod = periods[position];
            observeCategories();
        });
    }

    private void setupRecyclerView() {
        categoryAdapter = new CategoryAdapter();
        categoryAdapter.setDeleteListener(category ->
                Executors.newSingleThreadExecutor().execute(() ->
                        AppDatabase.getInstance(this).categoryDao().delete(category)));
        binding.categoriesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        binding.categoriesRecyclerView.setAdapter(categoryAdapter);
    }

    private void observeCategories() {
        AppDatabase.getInstance(this)
                .categoryDao()
                .getCategoriesByPeriodAndType(selectedPeriod, isIncomeStep)
                .observe(this, categories -> categoryAdapter.setCategoryList(categories));
    }

    private void updateStepUi() {
        if (isIncomeStep) {
            binding.stepTitleTextView.setText("Категории дохода");
            binding.totalIncomeTextView.setVisibility(android.view.View.GONE);
            binding.nextButton.setText("Далее → Расходы");
        } else {
            binding.stepTitleTextView.setText("Категории расходов");
            binding.totalIncomeTextView.setVisibility(android.view.View.VISIBLE);
            binding.totalIncomeTextView.setText(String.format("Доход: %.0f ₽", totalIncome));
            binding.nextButton.setText("Далее →");
        }
    }

    private void onNextClicked() {
        if (isIncomeStep) {
            Executors.newSingleThreadExecutor().execute(() -> {
                totalIncome = AppDatabase.getInstance(this)
                        .categoryDao().getTotalIncomeByPeriod(selectedPeriod);
                runOnUiThread(() -> {
                    if (totalIncome <= 0) {
                        android.widget.Toast.makeText(this,
                                "Добавьте хотя бы одну категорию дохода",
                                android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }
                    isIncomeStep = false;
                    observeCategories();
                    updateStepUi();
                });
            });
        } else {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit()
                    .putLong(KEY_START_TIME, System.currentTimeMillis())
                    .putString(KEY_PERIOD, selectedPeriod)
                    .apply();

            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("period", selectedPeriod);
            intent.putExtra("totalIncome", totalIncome);
            startActivity(intent);
        }
    }

    private void openAddCategorySheet() {
        AddCategoryBottomSheet.newInstance(selectedPeriod, isIncomeStep, totalIncome)
                .show(getSupportFragmentManager(), "AddCategoryBottomSheet");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}