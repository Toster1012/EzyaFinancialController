package com.example.ezya;

import androidx.room.ColumnInfo;

public class CategorySummary {

    @ColumnInfo(name = "categoryName")
    public String categoryName;

    @ColumnInfo(name = "categoryEmoji")
    public String categoryEmoji;

    @ColumnInfo(name = "amount")
    public double amount;

    public String getName() { return categoryName; }
    public String getEmoji() { return categoryEmoji; }
    public double getAmount() { return amount; }
}