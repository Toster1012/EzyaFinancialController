package com.example.ezya;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "period_records")
public class PeriodRecord {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String period;
    private long startTime;
    private long endTime;
    private double totalIncome;
    private double totalExpense;

    public PeriodRecord(String period, long startTime, long endTime,
                        double totalIncome, double totalExpense) {
        this.period = period;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getPeriod() { return period; }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
    public double getTotalIncome() { return totalIncome; }
    public double getTotalExpense() { return totalExpense; }
}