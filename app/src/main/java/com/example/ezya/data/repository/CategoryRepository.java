package com.example.ezya.data.repository;

import androidx.lifecycle.LiveData;
import com.example.ezya.data.db.CategoryDao;
import com.example.ezya.data.model.Category;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryRepository {

    private final CategoryDao dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public CategoryRepository(CategoryDao dao) {
        this.dao = dao;
    }

    public LiveData<List<Category>> getByPeriodAndType(String period, boolean isIncome) {
        return dao.getCategoriesByPeriodAndType(period, isIncome);
    }

    public List<Category> getByPeriodAndTypeSync(String period, boolean isIncome) {
        return dao.getCategoriesByPeriodAndTypeSync(period, isIncome);
    }

    public double getTotalIncomeByPeriod(String period) {
        return dao.getTotalIncomeByPeriod(period);
    }

    public double getTotalExpenseByPeriod(String period) {
        return dao.getTotalExpenseByPeriod(period);
    }

    public void insert(Category category, Runnable onDone) {
        executor.execute(() -> {
            dao.insert(category);
            if (onDone != null) onDone.run();
        });
    }

    public void update(Category category, Runnable onDone) {
        executor.execute(() -> {
            dao.update(category);
            if (onDone != null) onDone.run();
        });
    }

    public void delete(Category category) {
        executor.execute(() -> dao.delete(category));
    }

    public void deleteAll(Runnable onDone) {
        executor.execute(() -> {
            dao.deleteAll();
            if (onDone != null) onDone.run();
        });
    }

    public void getTotalIncome(String period, java.util.function.Consumer<Double> callback) {
        executor.execute(() -> callback.accept(dao.getTotalIncomeByPeriod(period)));
    }

    public void getTotalExpense(String period, java.util.function.Consumer<Double> callback) {
        executor.execute(() -> callback.accept(dao.getTotalExpenseByPeriod(period)));
    }
}