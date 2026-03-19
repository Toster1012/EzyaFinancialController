package com.example.ezya.ui.summary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ezya.App;
import com.example.ezya.R;
import com.example.ezya.base.BaseActivity;
import com.example.ezya.data.model.ArchivedTransaction;
import com.example.ezya.data.model.CategorySummary;
import com.example.ezya.databinding.ActivityHistorySummaryBinding;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;

public class HistorySummaryActivity extends BaseActivity {

    private ActivityHistorySummaryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistorySummaryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int periodId = getIntent().getIntExtra("periodId", -1);
        long startTime = getIntent().getLongExtra("startTime", 0);
        long endTime = getIntent().getLongExtra("endTime", 0);
        String period = getIntent().getStringExtra("period");

        binding.backButton.setOnClickListener(v -> finish());

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        binding.summaryDateRangeTextView.setText(
                sdf.format(new Date(startTime)) + " — " + sdf.format(new Date(endTime)));
        binding.summaryPeriodTypeTextView.setText(period);

        Executors.newSingleThreadExecutor().execute(() -> {
            var dao = App.from(this).container.database.archivedTransactionDao();

            List<CategorySummary> expenseSummaries =
                    dao.getExpenseSummaryByPeriod(periodId);
            List<CategorySummary> incomeSummaries =
                    dao.getIncomeSummaryByPeriod(periodId);

            double totalIncome = 0, totalExpense = 0;
            for (CategorySummary s : incomeSummaries) totalIncome += s.getAmount();
            for (CategorySummary s : expenseSummaries) totalExpense += s.getAmount();

            double saved = totalIncome - totalExpense;
            double finalTotalIncome = totalIncome;
            double finalTotalExpense = totalExpense;

            List<ResultItem> results = new ArrayList<>();
            for (CategorySummary s : expenseSummaries) {
                results.add(new ResultItem(
                        s.getCategoryEmoji(), s.getCategoryName(), s.getAmount(), false));
            }
            for (CategorySummary s : incomeSummaries) {
                results.add(new ResultItem(
                        s.getCategoryEmoji(), s.getCategoryName(), s.getAmount(), true));
            }

            runOnUiThread(() -> {
                binding.totalIncomeTextView.setText(
                        String.format("Доходы: %.0f ₽", finalTotalIncome));
                binding.totalExpenseTextView.setText(
                        String.format("Расходы: %.0f ₽", finalTotalExpense));

                if (saved >= 0) {
                    binding.savedTextView.setText(
                            String.format("Сэкономлено: %.0f ₽", saved));
                    binding.savedTextView.setTextColor(0xFF4CAF50);
                } else {
                    binding.savedTextView.setText(
                            String.format("Перерасход: %.0f ₽", Math.abs(saved)));
                    binding.savedTextView.setTextColor(0xFFFF5252);
                }

                ResultAdapter adapter = new ResultAdapter(results);
                binding.categoryResultsRecyclerView.setLayoutManager(
                        new LinearLayoutManager(this));
                binding.categoryResultsRecyclerView.setAdapter(adapter);
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    static class ResultItem {
        String emoji, name;
        double amount;
        boolean isIncome;

        ResultItem(String emoji, String name, double amount, boolean isIncome) {
            this.emoji = emoji;
            this.name = name;
            this.amount = amount;
            this.isIncome = isIncome;
        }
    }

    static class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.VH> {

        private final List<ResultItem> items;

        ResultAdapter(List<ResultItem> items) { this.items = items; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_category_result, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            ResultItem item = items.get(position);
            holder.emoji.setText(item.emoji);
            holder.name.setText(item.name);
            holder.planned.setText(item.isIncome ? "Доход" : "Расход");
            holder.actual.setVisibility(View.GONE);
            holder.diff.setText(String.format("%.0f ₽", item.amount));
            holder.diff.setTextColor(item.isIncome ? 0xFF4CAF50 : 0xFFFF5252);
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView emoji, name, planned, actual, diff;

            VH(@NonNull View v) {
                super(v);
                emoji = v.findViewById(R.id.resultEmojiTextView);
                name = v.findViewById(R.id.resultNameTextView);
                planned = v.findViewById(R.id.resultPlannedTextView);
                actual = v.findViewById(R.id.resultActualTextView);
                diff = v.findViewById(R.id.resultDiffTextView);
            }
        }
    }
}