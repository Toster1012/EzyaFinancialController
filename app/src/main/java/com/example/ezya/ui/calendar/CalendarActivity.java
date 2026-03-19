package com.example.ezya.ui.calendar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.ezya.App;
import com.example.ezya.R;
import com.example.ezya.base.BaseActivity;
import com.example.ezya.data.model.Transaction;
import com.example.ezya.databinding.ActivityCalendarBinding;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;

public class CalendarActivity extends BaseActivity {

    private static final String PREFS_NAME = "ezya_prefs";
    private static final String KEY_PERIOD = "selected_period";

    private ActivityCalendarBinding binding;
    private CalendarDayAdapter dayAdapter;
    private DayTransactionAdapter transactionAdapter;
    private Calendar currentMonth = Calendar.getInstance();
    private String currentPeriod;
    private List<Transaction> allTransactions = new ArrayList<>();
    private Map<String, List<Transaction>> transactionsByDay = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCalendarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentPeriod = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString(KEY_PERIOD, "Неделя");

        binding.backButton.setOnClickListener(v -> finish());
        binding.prevMonthButton.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            refreshCalendar();
        });
        binding.nextMonthButton.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            refreshCalendar();
        });

        setupRecyclers();
        loadTransactions();
    }

    private void setupRecyclers() {
        dayAdapter = new CalendarDayAdapter(day -> showDayDetail(day));
        binding.calendarGrid.setLayoutManager(new GridLayoutManager(this, 7));
        binding.calendarGrid.setAdapter(dayAdapter);

        transactionAdapter = new DayTransactionAdapter();
        binding.dayTransactionsList.setLayoutManager(new LinearLayoutManager(this));
        binding.dayTransactionsList.setAdapter(transactionAdapter);
    }

    private void loadTransactions() {
        App.from(this).container.transactionRepository
                .getByPeriodSync(currentPeriod, transactions -> {
                    allTransactions = transactions;
                    transactionsByDay.clear();
                    SimpleDateFormat sdf = new SimpleDateFormat(
                            "yyyy-MM-dd", Locale.getDefault());
                    for (Transaction t : allTransactions) {
                        String key = sdf.format(new Date(t.getTimestamp()));
                        if (!transactionsByDay.containsKey(key)) {
                            transactionsByDay.put(key, new ArrayList<>());
                        }
                        transactionsByDay.get(key).add(t);
                    }
                    runOnUiThread(this::refreshCalendar);
                });
    }

    private void refreshCalendar() {
        SimpleDateFormat monthFmt = new SimpleDateFormat("LLLL yyyy", Locale.getDefault());
        String title = monthFmt.format(currentMonth.getTime());
        binding.monthTitleTextView.setText(
                title.substring(0, 1).toUpperCase() + title.substring(1));

        Calendar cal = (Calendar) currentMonth.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int offset = (firstDayOfWeek == Calendar.SUNDAY) ? 6 : firstDayOfWeek - 2;
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        SimpleDateFormat keyFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar today = Calendar.getInstance();

        List<CalendarDayItem> items = new ArrayList<>();
        for (int i = 0; i < offset; i++) {
            items.add(new CalendarDayItem(0, null, false, false));
        }
        for (int d = 1; d <= daysInMonth; d++) {
            cal.set(Calendar.DAY_OF_MONTH, d);
            String key = keyFmt.format(cal.getTime());
            List<Transaction> dayTx = transactionsByDay.get(key);

            boolean hasIncome = false, hasExpense = false;
            if (dayTx != null) {
                for (Transaction t : dayTx) {
                    if (t.isExpense()) hasExpense = true;
                    else hasIncome = true;
                }
            }

            boolean isToday = cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                    && cal.get(Calendar.MONTH) == today.get(Calendar.MONTH)
                    && cal.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH);

            items.add(new CalendarDayItem(d, key, hasIncome, hasExpense, isToday,
                    dayTx != null && !dayTx.isEmpty()));
        }

        dayAdapter.setItems(items);
        binding.dayDetailCard.setVisibility(View.GONE);
    }

    private void showDayDetail(CalendarDayItem day) {
        if (day.day == 0 || !day.hasTransactions) {
            binding.dayDetailCard.setVisibility(View.GONE);
            return;
        }

        List<Transaction> txList = transactionsByDay.get(day.dateKey);
        if (txList == null || txList.isEmpty()) {
            binding.dayDetailCard.setVisibility(View.GONE);
            return;
        }

        SimpleDateFormat fmt = new SimpleDateFormat("d MMMM", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.set(currentMonth.get(Calendar.YEAR),
                currentMonth.get(Calendar.MONTH), day.day);
        binding.dayDetailTitleTextView.setText(fmt.format(cal.getTime()));

        double totalIncome = 0, totalExpense = 0;
        for (Transaction t : txList) {
            if (t.isExpense()) totalExpense += t.getAmount();
            else totalIncome += t.getAmount();
        }

        binding.dayTotalIncomeTextView.setText(String.format("+ %.0f ₽", totalIncome));
        binding.dayTotalExpenseTextView.setText(String.format("- %.0f ₽", totalExpense));

        double balance = totalIncome - totalExpense;
        binding.dayBalanceTextView.setText(String.format("%.0f ₽", balance));
        binding.dayBalanceTextView.setTextColor(
                balance >= 0 ? 0xFF4CAF50 : 0xFFFF5252);

        transactionAdapter.setList(txList);
        binding.dayDetailCard.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}