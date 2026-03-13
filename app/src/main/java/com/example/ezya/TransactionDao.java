package com.example.ezya;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface TransactionDao {

    @Insert
    void insert(Transaction transaction);

    @Query("SELECT * FROM transactions WHERE period = :period ORDER BY timestamp DESC")
    LiveData<List<Transaction>> getTransactionsByPeriod(String period);

    @Query("SELECT * FROM transactions WHERE period = :period AND isExpense = :isExpense ORDER BY timestamp DESC")
    LiveData<List<Transaction>> getTransactionsByPeriodAndType(String period, boolean isExpense);

    @Query("SELECT * FROM transactions WHERE period = :period ORDER BY timestamp DESC")
    List<Transaction> getTransactionsByPeriodSync(String period);

    @Query("SELECT categoryName, categoryEmoji, SUM(amount) as amount FROM transactions WHERE period = :period AND isExpense = 1 GROUP BY categoryName, categoryEmoji")
    LiveData<List<CategorySummary>> getExpenseSummaryByPeriod(String period);

    @Query("SELECT categoryName, categoryEmoji, SUM(amount) as amount FROM transactions WHERE period = :period AND isExpense = 0 GROUP BY categoryName, categoryEmoji")
    LiveData<List<CategorySummary>> getIncomeSummaryByPeriod(String period);

    @Query("DELETE FROM transactions")
    void deleteAll();
}