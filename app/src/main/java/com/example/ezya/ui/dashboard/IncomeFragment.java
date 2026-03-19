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
import com.example.ezya.databinding.FragmentIncomeBinding;
import com.example.ezya.ui.adapters.TransactionAdapter;

public class IncomeFragment extends Fragment {

    private FragmentIncomeBinding binding;
    private String currentPeriod;

    public static IncomeFragment newInstance(String period) {
        IncomeFragment f = new IncomeFragment();
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
        binding = FragmentIncomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) currentPeriod = getArguments().getString("period", "Неделя");

        TransactionAdapter adapter = new TransactionAdapter();
        binding.incomeTransactionsRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext()));
        binding.incomeTransactionsRecyclerView.setAdapter(adapter);
        binding.incomeTransactionsRecyclerView.setNestedScrollingEnabled(false);

        TransactionRepository repo = App.from(requireContext()).container.transactionRepository;

        repo.getIncomeSummary(currentPeriod).observe(getViewLifecycleOwner(), summaries -> {
            binding.incomeChartView.setSummaries(summaries);
            double total = 0;
            for (CategorySummary s : summaries) total += s.getAmount();
            binding.totalIncomeTextView.setText(String.format("Доходы: %.0f ₽", total));
        });

        repo.getByPeriodAndType(currentPeriod, false)
                .observe(getViewLifecycleOwner(), adapter::setTransactionList);

        binding.incomeChartView.setOnBarClickListener((emoji, name, amount) ->
                binding.selectedIncomeCategoryTextView.setText(
                        String.format("%s %s: %.0f ₽", emoji, name, amount)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}