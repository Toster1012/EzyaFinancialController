package com.example.ezya.ui.summary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ezya.App;
import com.example.ezya.base.BaseActivity;
import com.example.ezya.data.model.ArchivedTransaction;
import com.example.ezya.data.model.Category;
import com.example.ezya.data.model.PeriodRecord;
import com.example.ezya.data.model.Transaction;
import com.example.ezya.data.repository.CategoryRepository;
import com.example.ezya.data.repository.HistoryRepository;
import com.example.ezya.data.repository.TransactionRepository;
import com.example.ezya.databinding.ActivityPeriodSummaryBinding;
import com.example.ezya.ui.budget.BudgetActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PeriodSummaryActivity extends BaseActivity {

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
        CategoryRepository catRepo = App.from(this).container.categoryRepository;
        TransactionRepository txRepo = App.from(this).container.transactionRepository;

        txRepo.getByPeriodSync(currentPeriod, transactions -> {
            List<Category> incomeCategories =
                    catRepo.getByPeriodAndTypeSync(currentPeriod, true);
            List<Category> expenseCategories =
                    catRepo.getByPeriodAndTypeSync(currentPeriod, false);

            double totalPlannedIncome = 0;
            for (Category c : incomeCategories) totalPlannedIncome += c.getAmount();
            double totalPlannedExpense = 0;
            for (Category c : expenseCategories) totalPlannedExpense += c.getAmount();

            double totalActualIncome = 0, totalActualExpense = 0;
            Map<String, Double> expenseByCategory = new HashMap<>();
            for (Transaction t : transactions) {
                if (t.isExpense()) {
                    totalActualExpense += t.getAmount();
                    double cur = expenseByCategory.containsKey(t.getCategoryName())
                            ? expenseByCategory.get(t.getCategoryName()) : 0;
                    expenseByCategory.put(t.getCategoryName(), cur + t.getAmount());
                } else {
                    totalActualIncome += t.getAmount();
                }
            }

            List<ResultItem> results = new ArrayList<>();
            for (Category c : expenseCategories) {
                double actual = expenseByCategory.containsKey(c.getName())
                        ? expenseByCategory.get(c.getName()) : 0;
                results.add(new ResultItem(c.getEmoji(), c.getName(),
                        c.getAmount(), actual, c.getAmount() - actual));
            }

            double totalSaved = totalPlannedExpense - totalActualExpense;
            double finalIncome = totalActualIncome;
            double finalExpense = totalActualExpense;
            double finalPlanned = totalPlannedIncome;

            runOnUiThread(() -> {
                binding.plannedIncomeTextView.setText(
                        String.format("Запланированный доход: %.0f ₽", finalPlanned));
                binding.actualIncomeTextView.setText(
                        String.format("Доход: %.0f ₽", finalIncome));
                binding.actualExpenseTextView.setText(
                        String.format("Расход: %.0f ₽", finalExpense));
                if (totalSaved >= 0) {
                    binding.totalSavedTextView.setText(
                            String.format("Сэкономлено: %.0f ₽", totalSaved));
                    binding.totalSavedTextView.setTextColor(0xFF4CAF50);
                } else {
                    binding.totalSavedTextView.setText(
                            String.format("Перерасход: %.0f ₽", Math.abs(totalSaved)));
                    binding.totalSavedTextView.setTextColor(0xFFFF5252);
                }
                binding.categoryResultsRecyclerView.setLayoutManager(
                        new LinearLayoutManager(this));
                binding.categoryResultsRecyclerView.setAdapter(
                        new ResultAdapter(results));
            });
        });
    }

    private void archiveAndStartNew() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long startTime = prefs.getLong(KEY_START_TIME, System.currentTimeMillis());

        TransactionRepository txRepo = App.from(this).container.transactionRepository;
        HistoryRepository histRepo = App.from(this).container.historyRepository;

        txRepo.getByPeriodSync(currentPeriod, transactions -> {
            double totalIncome = 0, totalExpense = 0;
            for (Transaction t : transactions) {
                if (t.isExpense()) totalExpense += t.getAmount();
                else totalIncome += t.getAmount();
            }

            List<ArchivedTransaction> archived = new ArrayList<>();
            for (Transaction t : transactions) {
                archived.add(new ArchivedTransaction(0,
                        t.getCategoryName(), t.getCategoryEmoji(),
                        t.getAmount(), t.isExpense(),
                        t.getTimestamp(), t.getComment()));
            }

            PeriodRecord record = new PeriodRecord(currentPeriod, startTime,
                    System.currentTimeMillis(), totalIncome, totalExpense);

            histRepo.savePeriodWithTransactions(record, archived, () ->
                    txRepo.deleteAll(() -> runOnUiThread(() -> {
                        prefs.edit().putLong(KEY_START_TIME, 0)
                                .putString(KEY_PERIOD, currentPeriod).apply();
                        Intent intent = new Intent(this, BudgetActivity.class);
                        intent.putExtra("isNewPeriod", true);
                        intent.putExtra("period", currentPeriod);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }))
            );
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    static class ResultItem {
        String emoji, name;
        double planned, actual, diff;
        ResultItem(String e, String n, double p, double a, double d) {
            emoji=e; name=n; planned=p; actual=a; diff=d;
        }
    }

    static class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.VH> {
        private final List<ResultItem> items;
        ResultAdapter(List<ResultItem> items) { this.items = items; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(com.example.ezya.R.layout.item_category_result, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            ResultItem item = items.get(position);
            h.emoji.setText(item.emoji);
            h.name.setText(item.name);
            h.planned.setText(String.format("План: %.0f ₽", item.planned));
            h.actual.setText(String.format("Доход: %.0f ₽", item.actual));
            if (item.diff >= 0) {
                h.diff.setText(String.format("Сэкономлено: %.0f ₽", item.diff));
                h.diff.setTextColor(0xFF4CAF50);
            } else {
                h.diff.setText(String.format("Перерасход: %.0f ₽", Math.abs(item.diff)));
                h.diff.setTextColor(0xFFFF5252);
            }
        }

        @Override public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView emoji, name, planned, actual, diff;
            VH(@NonNull View v) {
                super(v);
                emoji = v.findViewById(com.example.ezya.R.id.resultEmojiTextView);
                name = v.findViewById(com.example.ezya.R.id.resultNameTextView);
                planned = v.findViewById(com.example.ezya.R.id.resultPlannedTextView);
                actual = v.findViewById(com.example.ezya.R.id.resultActualTextView);
                diff = v.findViewById(com.example.ezya.R.id.resultDiffTextView);
            }
        }
    }
}