package com.example.ezya;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface PeriodRecordDao {

    @Insert
    long insert(PeriodRecord record);

    @Query("SELECT * FROM period_records ORDER BY endTime DESC")
    LiveData<List<PeriodRecord>> getAllPeriods();

    @Query("SELECT * FROM period_records ORDER BY endTime DESC")
    List<PeriodRecord> getAllPeriodsSync();

    @Query("DELETE FROM period_records")
    void deleteAll();
}