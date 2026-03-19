package com.example.ezya.ui.budget;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.ezya.App;
import com.example.ezya.base.BaseActivity;
import com.example.ezya.data.model.Category;
import com.example.ezya.data.repository.CategoryRepository;
import com.example.ezya.databinding.ActivityBudgetBinding;
import com.example.ezya.ui.adapters.CategoryAdapter;
import com.example.ezya.ui.dashboard.DashboardActivity;
import java.util.List;

public class BudgetActivity extends BaseActivity {

    private static final String PREFS_NAME = "ezya_prefs";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_PERIOD = "selected_period";

    private ActivityBudgetBinding binding;
    private CategoryAdapter categoryAdapter;
    private CategoryRepository categoryRepo;
    private String selectedPeriod = "Неделя";
    private boolean isIncomeStep = true;
    private double totalIncome = 0;
    private boolean isNewPeriod = false;
    private LiveData<List<Category>> activeLiveData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBudgetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        categoryRepo = App.from(this).container.categoryRepository;

        isNewPeriod = getIntent().getBooleanExtra("isNewPeriod", false);
        if (isNewPeriod) {
            String savedPeriod = getIntent().getStringExtra("period");
            if (savedPeriod != null) selectedPeriod = savedPeriod;
        }

        setupPeriodDropdown();
        setupRecyclerView();

        if (isNewPeriod) {
            categoryRepo.getTotalIncome(selectedPeriod, total -> runOnUiThread(() -> {
                totalIncome = total;
                isIncomeStep = true;
                observeCategories();
                updateStepUi();
            }));
        } else {
            observeCategories();
            updateStepUi();
        }

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
        categoryAdapter.setDeleteListener(category -> categoryRepo.delete(category));
        categoryAdapter.setEditListener(category ->
                AddCategoryBottomSheet.newInstanceEdit(category, selectedPeriod, totalIncome)
                        .show(getSupportFragmentManager(), "EditCategory"));
        binding.categoriesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        binding.categoriesRecyclerView.setAdapter(categoryAdapter);
    }

    private void observeCategories() {
        if (activeLiveData != null) activeLiveData.removeObservers(this);
        activeLiveData = categoryRepo.getByPeriodAndType(selectedPeriod, isIncomeStep);
        activeLiveData.observe(this, categories -> {
            categoryAdapter.setCategoryList(categories);
            if (!isIncomeStep) {
                double totalExpense = 0;
                for (Category c : categories) totalExpense += c.getAmount();
                updateRemainingText(totalIncome - totalExpense);
            }
        });
    }

    private void updateRemainingText(double remaining) {
        if (remaining >= 0) {
            binding.remainingBudgetTextView.setText(
                    String.format("Остаток: %.0f ₽", remaining));
            binding.remainingBudgetTextView.setTextColor(0xFFFFDD2D);
        } else {
            binding.remainingBudgetTextView.setText(
                    String.format("Превышение: %.0f ₽", Math.abs(remaining)));
            binding.remainingBudgetTextView.setTextColor(0xFFFF5252);
        }
    }

    private void updateStepUi() {
        if (isIncomeStep) {
            binding.stepTitleTextView.setText("Категории дохода");
            binding.totalIncomeTextView.setVisibility(View.GONE);
            binding.remainingBudgetTextView.setVisibility(View.GONE);
            binding.nextButton.setText("Далее → Расходы");
        } else {
            binding.stepTitleTextView.setText("Категории расходов");
            binding.totalIncomeTextView.setVisibility(View.VISIBLE);
            binding.totalIncomeTextView.setText(String.format("Доход: %.0f ₽", totalIncome));
            binding.remainingBudgetTextView.setVisibility(View.VISIBLE);
            binding.nextButton.setText("Далее →");
        }
    }

    private void onNextClicked() {
        if (isIncomeStep) {
            categoryRepo.getTotalIncome(selectedPeriod, total -> runOnUiThread(() -> {
                totalIncome = total;
                if (totalIncome <= 0) {
                    android.widget.Toast.makeText(this,
                            "Добавьте хотя бы одну категорию дохода",
                            android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
                isIncomeStep = false;
                observeCategories();
                updateStepUi();
            }));
        } else {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit()
                    .putLong(KEY_START_TIME, System.currentTimeMillis())
                    .putString(KEY_PERIOD, selectedPeriod)
                    .apply();
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("period", selectedPeriod);
            startActivity(intent);
            finish();
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