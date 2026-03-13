package com.example.ezya;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ArchivedTransactionDao {

    @Insert
    void insert(ArchivedTransaction transaction);

    @Query("SELECT * FROM archived_transactions WHERE periodRecordId = :periodId AND isExpense = 0 ORDER BY timestamp DESC")
    List<ArchivedTransaction> getIncomeByPeriod(int periodId);

    @Query("SELECT * FROM archived_transactions WHERE periodRecordId = :periodId AND isExpense = 1 ORDER BY timestamp DESC")
    List<ArchivedTransaction> getExpenseByPeriod(int periodId);

    @Query("SELECT categoryName, categoryEmoji, SUM(amount) as amount FROM archived_transactions WHERE periodRecordId = :periodId AND isExpense = 0 GROUP BY categoryName, categoryEmoji")
    List<CategorySummary> getIncomeSummaryByPeriod(int periodId);

    @Query("SELECT categoryName, categoryEmoji, SUM(amount) as amount FROM archived_transactions WHERE periodRecordId = :periodId AND isExpense = 1 GROUP BY categoryName, categoryEmoji")
    List<CategorySummary> getExpenseSummaryByPeriod(int periodId);

    @Query("DELETE FROM archived_transactions")
    void deleteAll();
}