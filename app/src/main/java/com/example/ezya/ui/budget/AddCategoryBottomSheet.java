package com.example.ezya.ui.budget;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ezya.data.db.AppDatabase;
import com.example.ezya.R;
import com.example.ezya.data.model.Category;
import com.example.ezya.databinding.BottomSheetAddCategoryBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.concurrent.Executors;

public class AddCategoryBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetAddCategoryBinding binding;
    private String selectedEmoji = "📦";
    private String currentPeriod;
    private boolean isIncome;
    private double totalIncome;
    private Category editingCategory;

    private final String[] emojiOptions = {
            "🍕", "🚗", "🏠", "👕", "💊", "📚", "✈️", "🎮", "🐾", "💪",
            "☕", "🛒", "💄", "🎵", "🏋️", "📦", "💡", "🎁", "🍺", "🌿",
            "💼", "💰", "🏦", "📈", "🎓", "🔧", "🏪", "🎪"
    };

    public static AddCategoryBottomSheet newInstance(String period, boolean isIncome,
                                                     double totalIncome) {
        AddCategoryBottomSheet sheet = new AddCategoryBottomSheet();
        Bundle args = new Bundle();
        args.putString("period", period);
        args.putBoolean("isIncome", isIncome);
        args.putDouble("totalIncome", totalIncome);
        sheet.setArguments(args);
        return sheet;
    }

    public static AddCategoryBottomSheet newInstanceEdit(Category category,
                                                         String period, double totalIncome) {
        AddCategoryBottomSheet sheet = new AddCategoryBottomSheet();
        Bundle args = new Bundle();
        args.putString("period", period);
        args.putBoolean("isIncome", category.isIncome());
        args.putDouble("totalIncome", totalIncome);
        args.putInt("editId", category.getId());
        args.putString("editName", category.getName());
        args.putString("editEmoji", category.getEmoji());
        args.putDouble("editAmount", category.getAmount());
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomSheetAddCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            currentPeriod = getArguments().getString("period", "Неделя");
            isIncome = getArguments().getBoolean("isIncome", true);
            totalIncome = getArguments().getDouble("totalIncome", 0);

            int editId = getArguments().getInt("editId", -1);
            if (editId != -1) {
                editingCategory = new Category(
                        getArguments().getString("editName"),
                        getArguments().getString("editEmoji"),
                        getArguments().getDouble("editAmount"),
                        currentPeriod,
                        isIncome
                );
                editingCategory.setId(editId);

                selectedEmoji = editingCategory.getEmoji();
                binding.categoryNameEditText.setText(editingCategory.getName());
                binding.categoryAmountEditText.setText(
                        String.valueOf((int) editingCategory.getAmount()));
                binding.selectedEmojiTextView.setText(selectedEmoji);
                binding.sheetTitleTextView.setText(isIncome
                        ? "Редактировать доход" : "Редактировать расход");
                binding.addCategoryButton.setText("Сохранить");
            } else {
                binding.sheetTitleTextView.setText(isIncome
                        ? "Категория дохода" : "Категория расхода");
            }
        }

        buildEmojiGrid();
        binding.addCategoryButton.setOnClickListener(v -> saveCategory());
    }

    private void buildEmojiGrid() {
        for (String emoji : emojiOptions) {
            View emojiView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_emoji, binding.emojiGridLayout, false);
            android.widget.TextView tv = emojiView.findViewById(R.id.emojiItemTextView);
            tv.setText(emoji);
            tv.setOnClickListener(v -> {
                selectedEmoji = emoji;
                binding.selectedEmojiTextView.setText(emoji);
            });
            binding.emojiGridLayout.addView(emojiView);
        }
    }

    private void saveCategory() {
        String name = binding.categoryNameEditText.getText().toString().trim();
        String amountStr = binding.categoryAmountEditText.getText().toString().trim();

        if (name.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);

        if (!isIncome && totalIncome <= 0) {
            Toast.makeText(requireContext(),
                    "Сначала добавьте категории дохода", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            double alreadyAllocated = db.categoryDao()
                    .getTotalExpenseByPeriod(currentPeriod);

            if (editingCategory != null) {
                alreadyAllocated -= editingCategory.getAmount();
            }

            double remaining = totalIncome - alreadyAllocated;

            requireActivity().runOnUiThread(() -> {
                if (!isIncome && amount > remaining) {
                    Toast.makeText(requireContext(),
                            String.format("Превышение дохода. Доступно: %.0f ₽", remaining),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                Executors.newSingleThreadExecutor().execute(() -> {
                    if (editingCategory != null) {
                        editingCategory.setName(name);
                        editingCategory.setEmoji(selectedEmoji);
                        editingCategory.setAmount(amount);
                        db.categoryDao().update(editingCategory);
                    } else {
                        db.categoryDao().insert(
                                new Category(name, selectedEmoji, amount,
                                        currentPeriod, isIncome));
                    }
                    requireActivity().runOnUiThread(this::dismiss);
                });
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}