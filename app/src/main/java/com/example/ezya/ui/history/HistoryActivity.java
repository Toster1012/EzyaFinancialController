package com.example.ezya.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.ezya.data.db.AppDatabase;
import com.example.ezya.utils.BarChartView;
import com.example.ezya.R;
import com.example.ezya.data.db.ArchivedTransactionDao;
import com.example.ezya.data.model.ArchivedTransaction;
import com.example.ezya.data.model.CategorySummary;
import com.example.ezya.data.model.PeriodRecord;
import com.example.ezya.databinding.ActivityHistoryBinding;
import com.google.android.material.tabs.TabLayoutMediator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class HistoryActivity extends AppCompatActivity {

    private ActivityHistoryBinding binding;
    private HistoryPagerAdapter pagerAdapter;
    private final List<PeriodRecord> periodRecords = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backButton.setOnClickListener(v -> finish());

        pagerAdapter = new HistoryPagerAdapter(this, periodRecords);
        binding.historyViewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(binding.historyTabLayout, binding.historyViewPager,
                (tab, position) -> {
                    if (position < periodRecords.size()) {
                        PeriodRecord r = periodRecords.get(position);
                        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy", Locale.getDefault());
                        tab.setText(sdf.format(new Date(r.getEndTime())));
                    }
                }).attach();

        loadHistory();
    }

    private void loadHistory() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<PeriodRecord> records = AppDatabase.getInstance(this)
                    .periodRecordDao().getAllPeriodsSync();
            runOnUiThread(() -> {
                periodRecords.clear();
                periodRecords.addAll(records);
                pagerAdapter.notifyDataSetChanged();

                if (records.isEmpty()) {
                    binding.emptyHistoryTextView.setVisibility(View.VISIBLE);
                    binding.historyViewPager.setVisibility(View.GONE);
                    binding.historyTabLayout.setVisibility(View.GONE);
                } else {
                    binding.emptyHistoryTextView.setVisibility(View.GONE);
                    binding.historyViewPager.setVisibility(View.VISIBLE);
                    binding.historyTabLayout.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }


    static class HistoryPagerAdapter extends FragmentStateAdapter {
        private final List<PeriodRecord> records;

        HistoryPagerAdapter(@NonNull FragmentActivity fa, List<PeriodRecord> records) {
            super(fa);
            this.records = records;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return HistoryPeriodFragment.newInstance(records.get(position).getId());
        }

        @Override
        public int getItemCount() { return records.size(); }
    }


    public static class HistoryPeriodFragment extends Fragment {

        private int periodId;

        public static HistoryPeriodFragment newInstance(int periodId) {
            HistoryPeriodFragment f = new HistoryPeriodFragment();
            Bundle args = new Bundle();
            args.putInt("periodId", periodId);
            f.setArguments(args);
            return f;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) periodId = getArguments().getInt("periodId");
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_history_period, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            com.google.android.material.tabs.TabLayout tabLayout =
                    view.findViewById(R.id.historyPeriodTabLayout);
            ViewPager2 viewPager = view.findViewById(R.id.historyPeriodViewPager);

            HistoryTypeAdapter adapter = new HistoryTypeAdapter(
                    requireActivity(), periodId);
            viewPager.setAdapter(adapter);

            new TabLayoutMediator(tabLayout, viewPager,
                    (tab, position) ->
                            tab.setText(position == 0 ? "Доходы" : "Расходы")
            ).attach();
        }
    }


    static class HistoryTypeAdapter extends FragmentStateAdapter {
        private final int periodId;

        HistoryTypeAdapter(@NonNull FragmentActivity fa, int periodId) {
            super(fa);
            this.periodId = periodId;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return HistoryChartFragment.newInstance(periodId, position == 1);
        }

        @Override
        public int getItemCount() { return 2; }
    }


    public static class HistoryChartFragment extends Fragment {

        private int periodId;
        private boolean isExpense;

        public static HistoryChartFragment newInstance(int periodId, boolean isExpense) {
            HistoryChartFragment f = new HistoryChartFragment();
            Bundle args = new Bundle();
            args.putInt("periodId", periodId);
            args.putBoolean("isExpense", isExpense);
            f.setArguments(args);
            return f;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                periodId = getArguments().getInt("periodId");
                isExpense = getArguments().getBoolean("isExpense");
            }
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_history_chart, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            BarChartView chartView = view.findViewById(R.id.historyBarChartView);
            TextView totalTextView = view.findViewById(R.id.historyTotalTextView);
            TextView selectedTextView = view.findViewById(R.id.historySelectedTextView);
            RecyclerView recyclerView = view.findViewById(R.id.historyTransactionsList);

            ArchivedTransactionAdapter adapter = new ArchivedTransactionAdapter();
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            recyclerView.setAdapter(adapter);
            recyclerView.setNestedScrollingEnabled(false);

            Executors.newSingleThreadExecutor().execute(() -> {
                ArchivedTransactionDao dao = AppDatabase.getInstance(requireContext())
                        .archivedTransactionDao();

                List<CategorySummary> summaries = isExpense
                        ? dao.getExpenseSummaryByPeriod(periodId)
                        : dao.getIncomeSummaryByPeriod(periodId);

                List<ArchivedTransaction> transactions = isExpense
                        ? dao.getExpenseByPeriod(periodId)
                        : dao.getIncomeByPeriod(periodId);

                double total = 0;
                for (CategorySummary s : summaries) total += s.getAmount();
                double finalTotal = total;

                requireActivity().runOnUiThread(() -> {
                    chartView.setSummaries(summaries);
                    totalTextView.setText(String.format(
                            isExpense ? "Расходы: %.0f ₽" : "Доходы: %.0f ₽", finalTotal));
                    totalTextView.setTextColor(isExpense ? 0xFFFF5252 : 0xFF4CAF50);
                    adapter.setList(transactions);

                    chartView.setOnBarClickListener((emoji, name, amount) ->
                            selectedTextView.setText(
                                    String.format("%s %s: %.0f ₽", emoji, name, amount)));
                });
            });
        }
    }


    static class ArchivedTransactionAdapter extends
            RecyclerView.Adapter<ArchivedTransactionAdapter.VH> {

        private List<ArchivedTransaction> list = new ArrayList<>();

        void setList(List<ArchivedTransaction> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_transaction, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            ArchivedTransaction t = list.get(position);
            holder.emojiTextView.setText(t.getCategoryEmoji());
            holder.nameTextView.setText(t.getCategoryName());
            holder.commentTextView.setText(
                    t.getComment() != null ? t.getComment() : "");
            holder.commentTextView.setVisibility(
                    (t.getComment() != null && !t.getComment().isEmpty())
                            ? View.VISIBLE : View.GONE);

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM HH:mm", Locale.getDefault());
            holder.dateTextView.setText(sdf.format(new Date(t.getTimestamp())));

            String sign = t.isExpense() ? "-" : "+";
            int color = t.isExpense() ? 0xFFFF5252 : 0xFF4CAF50;
            holder.amountTextView.setText(String.format("%s%.0f ₽", sign, t.getAmount()));
            holder.amountTextView.setTextColor(color);
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView emojiTextView, nameTextView, dateTextView,
                    commentTextView, amountTextView;

            VH(@NonNull View itemView) {
                super(itemView);
                emojiTextView = itemView.findViewById(R.id.transactionEmojiTextView);
                nameTextView = itemView.findViewById(R.id.transactionNameTextView);
                dateTextView = itemView.findViewById(R.id.transactionDateTextView);
                commentTextView = itemView.findViewById(R.id.transactionCommentTextView);
                amountTextView = itemView.findViewById(R.id.transactionAmountTextView);
            }
        }
    }
}