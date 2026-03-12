package com.example.ezya;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ezya.databinding.BottomSheetAddCategoryBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.concurrent.Executors;

public class AddCategoryBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetAddCategoryBinding binding;
    private String selectedEmoji = "📦";
    private String currentPeriod;
    private boolean isIncome;
    private double totalIncome;

    private final String[] emojiOptions = {
            "🍕", "🚗", "🏠", "👕", "💊", "📚", "✈️", "🎮", "🐾", "💪",
            "☕", "🛒", "💄", "🎵", "🏋️", "📦", "💡", "🎁", "🍺", "🌿",
            "💼", "💰", "🏦", "📈", "🎓", "🔧", "🏪", "🎪"
    };

    public static AddCategoryBottomSheet newInstance(String period, boolean isIncome, double totalIncome) {
        AddCategoryBottomSheet sheet = new AddCategoryBottomSheet();
        Bundle args = new Bundle();
        args.putString("period", period);
        args.putBoolean("isIncome", isIncome);
        args.putDouble("totalIncome", totalIncome);
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
        }

        binding.sheetTitleTextView.setText(isIncome ? "Категория дохода" : "Категория расхода");
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
            Toast.makeText(requireContext(), "Сначала добавьте категории дохода", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            double alreadyAllocated = db.categoryDao().getTotalExpenseByPeriod(currentPeriod);
            double remaining = totalIncome - alreadyAllocated;

            requireActivity().runOnUiThread(() -> {
                if (!isIncome && amount > remaining) {
                    Toast.makeText(requireContext(),
                            String.format("Превышение дохода. Доступно: %.0f ₽", remaining),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                Category category = new Category(name, selectedEmoji, amount, currentPeriod, isIncome);
                Executors.newSingleThreadExecutor().execute(() -> {
                    db.categoryDao().insert(category);
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