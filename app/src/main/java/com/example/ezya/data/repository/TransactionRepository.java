package com.example.ezya.data.repository;

import androidx.lifecycle.LiveData;
import com.example.ezya.data.db.TransactionDao;
import com.example.ezya.data.model.CategorySummary;
import com.example.ezya.data.model.Transaction;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class TransactionRepository {

    private final TransactionDao dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public TransactionRepository(TransactionDao dao) {
        this.dao = dao;
    }

    public LiveData<List<Transaction>> getByPeriod(String period) {
        return dao.getTransactionsByPeriod(period);
    }

    public LiveData<List<Transaction>> getByPeriodAndType(String period, boolean isExpense) {
        return dao.getTransactionsByPeriodAndType(period, isExpense);
    }

    public void getByPeriodSync(String period, Consumer<List<Transaction>> callback) {
        executor.execute(() -> callback.accept(dao.getTransactionsByPeriodSync(period)));
    }

    public LiveData<List<CategorySummary>> getExpenseSummary(String period) {
        return dao.getExpenseSummaryByPeriod(period);
    }

    public LiveData<List<CategorySummary>> getIncomeSummary(String period) {
        return dao.getIncomeSummaryByPeriod(period);
    }

    public void insert(Transaction transaction, Runnable onDone) {
        executor.execute(() -> {
            dao.insert(transaction);
            if (onDone != null) onDone.run();
        });
    }

    public void deleteAll(Runnable onDone) {
        executor.execute(() -> {
            dao.deleteAll();
            if (onDone != null) onDone.run();
        });
    }
}