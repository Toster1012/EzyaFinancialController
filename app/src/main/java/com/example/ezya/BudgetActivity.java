package com.example.ezya;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.ezya.databinding.ActivityBudgetBinding;
import java.util.concurrent.Executors;

public class BudgetActivity extends AppCompatActivity {

    private ActivityBudgetBinding binding;
    private CategoryAdapter categoryAdapter;
    private String selectedPeriod = "Неделя";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBudgetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupPeriodDropdown();
        setupRecyclerView();
        observeCategories();

        binding.addCategoryButton.setOnClickListener(v -> openAddCategorySheet());
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
                .getCategoriesByPeriod(selectedPeriod)
                .observe(this, categories -> categoryAdapter.setCategoryList(categories));
    }

    private double getCurrentBudget() {
        String budgetStr = binding.budgetEditText.getText().toString().trim();
        if (budgetStr.isEmpty()) return 0;
        try {
            return Double.parseDouble(budgetStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void openAddCategorySheet() {
        AddCategoryBottomSheet.newInstance(selectedPeriod, getCurrentBudget())
                .show(getSupportFragmentManager(), "AddCategoryBottomSheet");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}