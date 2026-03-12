package com.example.ezya;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface CategoryDao {

    @Insert
    void insert(Category category);

    @Query("SELECT * FROM categories WHERE period = :period")
    LiveData<List<Category>> getCategoriesByPeriod(String period);
}