package com.example.ezya.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.ezya.App;
import com.example.ezya.data.model.CategorySummary;
import com.example.ezya.data.repository.TransactionRepository;
import com.example.ezya.databinding.FragmentExpenseBinding;
import com.example.ezya.ui.adapters.TransactionAdapter;

public class ExpenseFragment extends Fragment {

    private FragmentExpenseBinding binding;
    private String currentPeriod;

    public static ExpenseFragment newInstance(String period) {
        ExpenseFragment f = new ExpenseFragment();
        Bundle args = new Bundle();
        args.putString("period", period);
        f.setArguments(args);
        return f;
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
        if (getArguments() != null) currentPeriod = getArguments().getString("period", "Неделя");

        TransactionAdapter adapter = new TransactionAdapter();
        binding.expenseTransactionsRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext()));
        binding.expenseTransactionsRecyclerView.setAdapter(adapter);
        binding.expenseTransactionsRecyclerView.setNestedScrollingEnabled(false);

        TransactionRepository repo = App.from(requireContext()).container.transactionRepository;

        repo.getExpenseSummary(currentPeriod).observe(getViewLifecycleOwner(), summaries -> {
            binding.expenseChartView.setSummaries(summaries);
            double total = 0;
            for (CategorySummary s : summaries) total += s.getAmount();
            binding.totalExpenseTextView.setText(String.format("Расходы: %.0f ₽", total));
        });

        repo.getByPeriodAndType(currentPeriod, true)
                .observe(getViewLifecycleOwner(), adapter::setTransactionList);

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