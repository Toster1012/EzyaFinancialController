package com.example.ezya.data.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.ezya.data.model.ArchivedTransaction;
import com.example.ezya.data.model.Category;
import com.example.ezya.data.model.PeriodRecord;
import com.example.ezya.data.model.Transaction;

@Database(
        entities = {
                Category.class,
                Transaction.class,
                PeriodRecord.class,
                ArchivedTransaction.class
        },
        version = 4,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract CategoryDao categoryDao();
    public abstract TransactionDao transactionDao();
    public abstract PeriodRecordDao periodRecordDao();
    public abstract ArchivedTransactionDao archivedTransactionDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "ezya_db"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}