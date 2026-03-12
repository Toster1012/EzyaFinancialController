package com.example.ezya;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class Transaction {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String categoryName;
    private String categoryEmoji;
    private double amount;
    private boolean isExpense;
    private String period;
    private long timestamp;

    public Transaction(String categoryName, String categoryEmoji, double amount,
                       boolean isExpense, String period, long timestamp) {
        this.categoryName = categoryName;
        this.categoryEmoji = categoryEmoji;
        this.amount = amount;
        this.isExpense = isExpense;
        this.period = period;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCategoryName() { return categoryName; }
    public String getCategoryEmoji() { return categoryEmoji; }
    public double getAmount() { return amount; }
    public boolean isExpense() { return isExpense; }
    public String getPeriod() { return period; }
    public long getTimestamp() { return timestamp; }
}