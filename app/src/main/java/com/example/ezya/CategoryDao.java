package com.example.ezya;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface CategoryDao {

    @Insert
    void insert(Category category);

    @Update
    void update(Category category);

    @Delete
    void delete(Category category);

    @Query("SELECT * FROM categories WHERE period = :period AND isIncome = :isIncome")
    LiveData<List<Category>> getCategoriesByPeriodAndType(String period, boolean isIncome);

    @Query("SELECT * FROM categories WHERE period = :period AND isIncome = :isIncome")
    List<Category> getCategoriesByPeriodAndTypeSync(String period, boolean isIncome);

    @Query("SELECT SUM(amount) FROM categories WHERE period = :period AND isIncome = 1")
    double getTotalIncomeByPeriod(String period);

    @Query("SELECT SUM(amount) FROM categories WHERE period = :period AND isIncome = 0")
    double getTotalExpenseByPeriod(String period);

    @Query("DELETE FROM categories")
    void deleteAll();
}