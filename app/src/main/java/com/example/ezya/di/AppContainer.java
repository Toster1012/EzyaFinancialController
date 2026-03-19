package com.example.ezya.di;

import android.content.Context;
import com.example.ezya.data.db.AppDatabase;
import com.example.ezya.data.repository.CategoryRepository;
import com.example.ezya.data.repository.HistoryRepository;
import com.example.ezya.data.repository.TransactionRepository;

public class AppContainer {

    public final AppDatabase database;
    public final CategoryRepository categoryRepository;
    public final TransactionRepository transactionRepository;
    public final HistoryRepository historyRepository;

    public AppContainer(Context context) {
        database = AppDatabase.build(context);
        categoryRepository = new CategoryRepository(database.categoryDao());
        transactionRepository = new TransactionRepository(database.transactionDao());
        historyRepository = new HistoryRepository(
                database.periodRecordDao(),
                database.archivedTransactionDao()
        );
    }
}