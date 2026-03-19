package com.example.ezya.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ezya.data.db.AppDatabase;
import com.example.ezya.data.model.CategorySummary;
import com.example.ezya.databinding.FragmentExpenseBinding;
import com.example.ezya.ui.adapters.TransactionAdapter;

public class ExpenseFragment extends Fragment {

    private FragmentExpenseBinding binding;
    private String currentPeriod;

    public static ExpenseFragment newInstance(String period) {
        ExpenseFragment fragment = new ExpenseFragment();
        Bundle args = new Bundle();
        args.putString("period", period);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentExpenseBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            currentPeriod = getArguments().getString("period", "Неделя");
        }

        TransactionAdapter transactionAdapter = new TransactionAdapter();
        binding.expenseTransactionsRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext()));
        binding.expenseTransactionsRecyclerView.setAdapter(transactionAdapter);
        binding.expenseTransactionsRecyclerView.setNestedScrollingEnabled(false);

        AppDatabase db = AppDatabase.getInstance(requireContext());

        db.transactionDao().getExpenseSummaryByPeriod(currentPeriod)
                .observe(getViewLifecycleOwner(), summaries -> {
                    binding.expenseChartView.setSummaries(summaries);
                    double total = 0;
                    for (CategorySummary s : summaries) total += s.getAmount();
                    binding.totalExpenseTextView.setText(
                            String.format("Расходы: %.0f ₽", total));
                });

        db.transactionDao().getTransactionsByPeriodAndType(currentPeriod, true)
                .observe(getViewLifecycleOwner(),
                        transactionAdapter::setTransactionList);

        binding.expenseChartView.setOnBarClickListener((emoji, name, amount) ->
                binding.selectedExpenseCategoryTextView.setText(
                        String.format("%s %s: %.0f ₽", emoji, name, amount)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}