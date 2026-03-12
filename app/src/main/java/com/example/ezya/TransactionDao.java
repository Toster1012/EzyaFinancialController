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

    @Query("DELETE FROM transactions WHERE period = :period")
    void deleteAllByPeriod(String period);

    @Query("DELETE FROM transactions")
    void deleteAll();
}