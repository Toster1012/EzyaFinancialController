package com.example.ezya;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class Category {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String emoji;
    private double amount;
    private String period;
    private boolean isIncome;

    public Category(String name, String emoji, double amount,
                    String period, boolean isIncome) {
        this.name = name;
        this.emoji = emoji;
        this.amount = amount;
        this.period = period;
        this.isIncome = isIncome;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getPeriod() { return period; }
    public boolean isIncome() { return isIncome; }
}