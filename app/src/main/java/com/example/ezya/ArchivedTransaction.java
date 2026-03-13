package com.example.ezya;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "archived_transactions")
public class ArchivedTransaction {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int periodRecordId;
    private String categoryName;
    private String categoryEmoji;
    private double amount;
    private boolean isExpense;
    private long timestamp;
    private String comment;

    public ArchivedTransaction(int periodRecordId, String categoryName,
                               String categoryEmoji, double amount,
                               boolean isExpense, long timestamp, String comment) {
        this.periodRecordId = periodRecordId;
        this.categoryName = categoryName;
        this.categoryEmoji = categoryEmoji;
        this.amount = amount;
        this.isExpense = isExpense;
        this.timestamp = timestamp;
        this.comment = comment;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPeriodRecordId() { return periodRecordId; }
    public String getCategoryName() { return categoryName; }
    public String getCategoryEmoji() { return categoryEmoji; }
    public double getAmount() { return amount; }
    public boolean isExpense() { return isExpense; }
    public long getTimestamp() { return timestamp; }
    public String getComment() { return comment; }
}