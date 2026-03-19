package com.example.ezya.data.repository;

import androidx.lifecycle.LiveData;
import com.example.ezya.data.db.ArchivedTransactionDao;
import com.example.ezya.data.db.PeriodRecordDao;
import com.example.ezya.data.model.ArchivedTransaction;
import com.example.ezya.data.model.CategorySummary;
import com.example.ezya.data.model.PeriodRecord;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class HistoryRepository {

    private final PeriodRecordDao periodDao;
    private final ArchivedTransactionDao archiveDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public HistoryRepository(PeriodRecordDao periodDao, ArchivedTransactionDao archiveDao) {
        this.periodDao = periodDao;
        this.archiveDao = archiveDao;
    }

    public LiveData<List<PeriodRecord>> getAllPeriods() {
        return periodDao.getAllPeriods();
    }

    public void getAllPeriodsSync(Consumer<List<PeriodRecord>> callback) {
        executor.execute(() -> callback.accept(periodDao.getAllPeriodsSync()));
    }

    public void savePeriodWithTransactions(PeriodRecord record,
                                           List<ArchivedTransaction> transactions,
                                           Runnable onDone) {
        executor.execute(() -> {
            long recordId = periodDao.insert(record);
            for (ArchivedTransaction t : transactions) {
                ArchivedTransaction copy = new ArchivedTransaction(
                        (int) recordId,
                        t.getCategoryName(),
                        t.getCategoryEmoji(),
                        t.getAmount(),
                        t.isExpense(),
                        t.getTimestamp(),
                        t.getComment()
                );
                archiveDao.insert(copy);
            }
            if (onDone != null) onDone.run();
        });
    }

    public void getIncomeSummary(int periodId, Consumer<List<CategorySummary>> callback) {
        executor.execute(() -> callback.accept(archiveDao.getIncomeSummaryByPeriod(periodId)));
    }

    public void getExpenseSummary(int periodId, Consumer<List<CategorySummary>> callback) {
        executor.execute(() -> callback.accept(archiveDao.getExpenseSummaryByPeriod(periodId)));
    }

    public void getIncomeTransactions(int periodId, Consumer<List<ArchivedTransaction>> callback) {
        executor.execute(() -> callback.accept(archiveDao.getIncomeByPeriod(periodId)));
    }

    public void getExpenseTransactions(int periodId, Consumer<List<ArchivedTransaction>> callback) {
        executor.execute(() -> callback.accept(archiveDao.getExpenseByPeriod(periodId)));
    }

    public void deleteAll(Runnable onDone) {
        executor.execute(() -> {
            periodDao.deleteAll();
            archiveDao.deleteAll();
            if (onDone != null) onDone.run();
        });
    }
}