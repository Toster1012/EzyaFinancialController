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

    public Category(String name, String emoji, double amount, String period) {
        this.name = name;
        this.emoji = emoji;
        this.amount = amount;
        this.period = period;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public String getEmoji() { return emoji; }
    public double getAmount() { return amount; }
    public String getPeriod() { return period; }
}