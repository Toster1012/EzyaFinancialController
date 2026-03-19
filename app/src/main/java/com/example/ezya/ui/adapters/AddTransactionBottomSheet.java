package com.example.ezya.ui.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ezya.App;
import com.example.ezya.data.model.Category;
import com.example.ezya.data.model.Transaction;
import com.example.ezya.data.repository.CategoryRepository;
import com.example.ezya.data.repository.TransactionRepository;
import com.example.ezya.databinding.BottomSheetAddTransactionBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import java.util.List;

public class AddTransactionBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetAddTransactionBinding binding;
    private String currentPeriod;
    private boolean forceExpense;
    private List<Category> incomeCategories = new ArrayList<>();
    private List<Category> expenseCategories = new ArrayList<>();

    public static AddTransactionBottomSheet newInstance(String period, boolean isExpense) {
        AddTransactionBottomSheet sheet = new AddTransactionBottomSheet();
        Bundle args = new Bundle();
        args.putString("period", period);
        args.putBoolean("forceExpense", isExpense);
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomSheetAddTransactionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            currentPeriod = getArguments().getString("period", "Неделя");
            forceExpense = getArguments().getBoolean("forceExpense", false);
        }

        if (forceExpense) {
            binding.expenseRadioButton.setChecked(true);
            binding.incomeRadioButton.setEnabled(false);
        } else {
            binding.incomeRadioButton.setChecked(true);
            binding.expenseRadioButton.setEnabled(false);
        }

        CategoryRepository categoryRepo = App.from(requireContext()).container.categoryRepository;

        categoryRepo.getByPeriodAndType(currentPeriod, false)
                .observe(getViewLifecycleOwner(), categories -> {
                    expenseCategories = categories;
                    if (binding.expenseRadioButton.isChecked()) refreshSpinner(expenseCategories);
                });

        categoryRepo.getByPeriodAndType(currentPeriod, true)
                .observe(getViewLifecycleOwner(), categories -> {
                    incomeCategories = categories;
                    if (binding.incomeRadioButton.isChecked()) refreshSpinner(incomeCategories);
                });

        binding.expenseRadioButton.setOnClickListener(v -> refreshSpinner(expenseCategories));
        binding.incomeRadioButton.setOnClickListener(v -> refreshSpinner(incomeCategories));
        binding.saveTransactionButton.setOnClickListener(v -> saveTransaction());
    }

    private void refreshSpinner(List<Category> categories) {
        List<String> names = new ArrayList<>();
        for (Category c : categories) names.add(c.getEmoji() + " " + c.getName());
        binding.categorySpinner.setAdapter(new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, names));
    }

    private void saveTransaction() {
        int selectedIndex = binding.categorySpinner.getSelectedItemPosition();
        String amountStr = binding.transactionAmountEditText.getText().toString().trim();
        String comment = binding.commentEditText.getText().toString().trim();
        boolean isExpense = binding.expenseRadioButton.isChecked();
        List<Category> currentCategories = isExpense ? expenseCategories : incomeCategories;

        if (amountStr.isEmpty()) {
            Toast.makeText(requireContext(), "Введите сумму", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentCategories.isEmpty() || selectedIndex < 0) {
            Toast.makeText(requireContext(), "Нет категорий", Toast.LENGTH_SHORT).show();
            return;
        }

        Category selected = currentCategories.get(selectedIndex);
        Transaction transaction = new Transaction(
                selected.getName(), selected.getEmoji(),
                Double.parseDouble(amountStr), isExpense,
                currentPeriod, System.currentTimeMillis(), comment
        );

        TransactionRepository repo = App.from(requireContext()).container.transactionRepository;
        repo.insert(transaction, this::dismiss);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}