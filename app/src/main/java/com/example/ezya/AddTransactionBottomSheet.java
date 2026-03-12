package com.example.ezya;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ezya.databinding.BottomSheetAddTransactionBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class AddTransactionBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetAddTransactionBinding binding;
    private String currentPeriod;
    private List<Category> categories = new ArrayList<>();

    public static AddTransactionBottomSheet newInstance(String period) {
        AddTransactionBottomSheet sheet = new AddTransactionBottomSheet();
        Bundle args = new Bundle();
        args.putString("period", period);
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
        }

        loadCategories();
        binding.saveTransactionButton.setOnClickListener(v -> saveTransaction());
    }

    private void loadCategories() {
        AppDatabase.getInstance(requireContext())
                .categoryDao()
                .getCategoriesByPeriod(currentPeriod)
                .observe(getViewLifecycleOwner(), categoryList -> {
                    categories = categoryList;
                    List<String> names = new ArrayList<>();
                    for (Category c : categoryList) {
                        names.add(c.getEmoji() + " " + c.getName());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            names);
                    binding.categorySpinner.setAdapter(adapter);
                });
    }

    private void saveTransaction() {
        int selectedIndex = binding.categorySpinner.getSelectedItemPosition();
        String amountStr = binding.transactionAmountEditText.getText().toString().trim();
        boolean isExpense = binding.expenseRadioButton.isChecked();

        if (amountStr.isEmpty()) {
            Toast.makeText(requireContext(), "Введите сумму", Toast.LENGTH_SHORT).show();
            return;
        }

        if (categories.isEmpty() || selectedIndex < 0) {
            Toast.makeText(requireContext(), "Выберите категорию", Toast.LENGTH_SHORT).show();
            return;
        }

        Category selectedCategory = categories.get(selectedIndex);
        double amount = Double.parseDouble(amountStr);

        Transaction transaction = new Transaction(
                selectedCategory.getName(),
                selectedCategory.getEmoji(),
                amount,
                isExpense,
                currentPeriod,
                System.currentTimeMillis()
        );

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(requireContext()).transactionDao().insert(transaction);
            requireActivity().runOnUiThread(this::dismiss);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}