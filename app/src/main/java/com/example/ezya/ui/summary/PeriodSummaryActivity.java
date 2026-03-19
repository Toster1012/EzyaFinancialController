package com.example.ezya.ui.summary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezya.data.db.AppDatabase;
import com.example.ezya.R;
import com.example.ezya.data.model.ArchivedTransaction;
import com.example.ezya.data.model.Category;
import com.example.ezya.data.model.PeriodRecord;
import com.example.ezya.data.model.Transaction;
import com.example.ezya.databinding.ActivityPeriodSummaryBinding;
import com.example.ezya.ui.budget.BudgetActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class PeriodSummaryActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "ezya_prefs";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_PERIOD = "selected_period";

    private ActivityPeriodSummaryBinding binding;
    private String currentPeriod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPeriodSummaryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentPeriod = getIntent().getStringExtra("period");

        loadSummary();
        binding.newPeriodButton.setOnClickListener(v -> archiveAndStartNew());
    }

    private void loadSummary() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);

            List<Category> incomeCategories = db.categoryDao()
                    .getCategoriesByPeriodAndTypeSync(currentPeriod, true);
            List<Category> expenseCategories = db.categoryDao()
                    .getCategoriesByPeriodAndTypeSync(currentPeriod, false);
            List<Transaction> transactions = db.transactionDao()
                    .getTransactionsByPeriodSync(currentPeriod);

            double totalPlannedIncome = 0;
            for (Category c : incomeCategories) totalPlannedIncome += c.getAmount();

            double totalPlannedExpense = 0;
            for (Category c : expenseCategories) totalPlannedExpense += c.getAmount();

            double totalActualIncome = 0;
            double totalActualExpense = 0;
            Map<String, Double> actualExpenseByCategory = new HashMap<>();

            for (Transaction t : transactions) {
                if (t.isExpense()) {
                    totalActualExpense += t.getAmount();
                    double current = actualExpenseByCategory.containsKey(t.getCategoryName())
                            ? actualExpenseByCategory.get(t.getCategoryName()) : 0;
                    actualExpenseByCategory.put(t.getCategoryName(), current + t.getAmount());
                } else {
                    totalActualIncome += t.getAmount();
                }
            }

            List<CategoryResultItem> results = new ArrayList<>();
            for (Category c : expenseCategories) {
                double planned = c.getAmount();
                double actual = actualExpenseByCategory.containsKey(c.getName())
                        ? actualExpenseByCategory.get(c.getName()) : 0;
                double diff = planned - actual;
                results.add(new CategoryResultItem(
                        c.getEmoji(), c.getName(), planned, actual, diff));
            }

            double totalSaved = totalPlannedExpense - totalActualExpense;
            double finalTotalPlannedIncome = totalPlannedIncome;
            double finalTotalActualIncome = totalActualIncome;
            double finalTotalActualExpense = totalActualExpense;

            runOnUiThread(() -> {
                binding.plannedIncomeTextView.setText(
                        String.format("Запланированный доход: %.0f ₽", finalTotalPlannedIncome));
                binding.actualIncomeTextView.setText(
                        String.format("Доход: %.0f ₽", finalTotalActualIncome));
                binding.actualExpenseTextView.setText(
                        String.format("Расход: %.0f ₽", finalTotalActualExpense));

                if (totalSaved >= 0) {
                    binding.totalSavedTextView.setText(
                            String.format("Сэкономлено: %.0f ₽", totalSaved));
                    binding.totalSavedTextView.setTextColor(0xFF4CAF50);
                } else {
                    binding.totalSavedTextView.setText(
                            String.format("Перерасход: %.0f ₽", Math.abs(totalSaved)));
                    binding.totalSavedTextView.setTextColor(0xFFFF5252);
                }

                CategoryResultAdapter adapter = new CategoryResultAdapter(results);
                binding.categoryResultsRecyclerView.setLayoutManager(
                        new LinearLayoutManager(this));
                binding.categoryResultsRecyclerView.setAdapter(adapter);
            });
        });
    }

    private void archiveAndStartNew() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            long startTime = prefs.getLong(KEY_START_TIME, System.currentTimeMillis());
            long endTime = System.currentTimeMillis();

            List<Transaction> transactions = db.transactionDao()
                    .getTransactionsByPeriodSync(currentPeriod);

            double totalIncome = 0, totalExpense = 0;
            for (Transaction t : transactions) {
                if (t.isExpense()) totalExpense += t.getAmount();
                else totalIncome += t.getAmount();
            }

            PeriodRecord record = new PeriodRecord(
                    currentPeriod, startTime, endTime, totalIncome, totalExpense);
            long recordId = db.periodRecordDao().insert(record);

            for (Transaction t : transactions) {
                db.archivedTransactionDao().insert(new ArchivedTransaction(
                        (int) recordId,
                        t.getCategoryName(),
                        t.getCategoryEmoji(),
                        t.getAmount(),
                        t.isExpense(),
                        t.getTimestamp(),
                        t.getComment()
                ));
            }

            db.transactionDao().deleteAll();

            runOnUiThread(() -> {
                prefs.edit()
                        .putLong(KEY_START_TIME, 0)
                        .putString(KEY_PERIOD, currentPeriod)
                        .apply();

                Intent intent = new Intent(this, BudgetActivity.class);
                intent.putExtra("isNewPeriod", true);
                intent.putExtra("period", currentPeriod);
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

    static class CategoryResultItem {
        String emoji, name;
        double planned, actual, diff;

        CategoryResultItem(String emoji, String name,
                           double planned, double actual, double diff) {
            this.emoji = emoji;
            this.name = name;
            this.planned = planned;
            this.actual = actual;
            this.diff = diff;
        }
    }

    static class CategoryResultAdapter extends
            RecyclerView.Adapter<CategoryResultAdapter.ResultViewHolder> {

        private final List<CategoryResultItem> items;

        CategoryResultAdapter(List<CategoryResultItem> items) { this.items = items; }

        @NonNull
        @Override
        public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_category_result, parent, false);
            return new ResultViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
            CategoryResultItem item = items.get(position);
            holder.emojiTextView.setText(item.emoji);
            holder.nameTextView.setText(item.name);
            holder.plannedTextView.setText(String.format("План: %.0f ₽", item.planned));
            holder.actualTextView.setText(String.format("Доход: %.0f ₽", item.actual));
            if (item.diff >= 0) {
                holder.diffTextView.setText(String.format("Сэкономлено: %.0f ₽", item.diff));
                holder.diffTextView.setTextColor(0xFF4CAF50);
            } else {
                holder.diffTextView.setText(String.format("Перерасход: %.0f ₽", Math.abs(item.diff)));
                holder.diffTextView.setTextColor(0xFFFF5252);
            }
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class ResultViewHolder extends RecyclerView.ViewHolder {
            TextView emojiTextView, nameTextView, plannedTextView,
                    actualTextView, diffTextView;

            ResultViewHolder(@NonNull View itemView) {
                super(itemView);
                emojiTextView = itemView.findViewById(R.id.resultEmojiTextView);
                nameTextView = itemView.findViewById(R.id.resultNameTextView);
                plannedTextView = itemView.findViewById(R.id.resultPlannedTextView);
                actualTextView = itemView.findViewById(R.id.resultActualTextView);
                diffTextView = itemView.findViewById(R.id.resultDiffTextView);
            }
        }
    }
}