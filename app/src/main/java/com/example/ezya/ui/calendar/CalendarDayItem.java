package com.example.ezya.ui.calendar;

public class CalendarDayItem {
    public int day;
    public String dateKey;
    public boolean hasIncome;
    public boolean hasExpense;
    public boolean isToday;
    public boolean hasTransactions;

    public CalendarDayItem(int day, String dateKey,
                           boolean hasIncome, boolean hasExpense) {
        this.day = day;
        this.dateKey = dateKey;
        this.hasIncome = hasIncome;
        this.hasExpense = hasExpense;
    }

    public CalendarDayItem(int day, String dateKey,
                           boolean hasIncome, boolean hasExpense,
                           boolean isToday, boolean hasTransactions) {
        this.day = day;
        this.dateKey = dateKey;
        this.hasIncome = hasIncome;
        this.hasExpense = hasExpense;
        this.isToday = isToday;
        this.hasTransactions = hasTransactions;
    }
}