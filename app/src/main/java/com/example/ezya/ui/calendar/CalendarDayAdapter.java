package com.example.ezya.ui.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ezya.R;
import java.util.ArrayList;
import java.util.List;

public class CalendarDayAdapter extends
        RecyclerView.Adapter<CalendarDayAdapter.DayVH> {

    public interface OnDayClickListener {
        void onClick(CalendarDayItem day);
    }

    private List<CalendarDayItem> items = new ArrayList<>();
    private final OnDayClickListener listener;

    public CalendarDayAdapter(OnDayClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<CalendarDayItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new DayVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DayVH holder, int position) {
        CalendarDayItem item = items.get(position);

        if (item.day == 0) {
            holder.dayText.setText("");
            holder.incomeIndicator.setVisibility(View.INVISIBLE);
            holder.expenseIndicator.setVisibility(View.INVISIBLE);
            holder.itemView.setBackground(null);
            return;
        }

        holder.dayText.setText(String.valueOf(item.day));

        if (item.isToday) {
            holder.dayText.setTextColor(0xFF0A0A0A);
            holder.itemView.setBackgroundResource(R.drawable.calendar_today_bg);
        } else if (item.hasTransactions) {
            holder.dayText.setTextColor(0xFFFFDD2D);
            holder.itemView.setBackgroundResource(R.drawable.calendar_active_day_bg);
        } else {
            holder.dayText.setTextColor(0xFFFFFFFF);
            holder.itemView.setBackgroundResource(R.drawable.calendar_day_bg);
        }

        holder.incomeIndicator.setVisibility(
                item.hasIncome ? View.VISIBLE : View.INVISIBLE);
        holder.expenseIndicator.setVisibility(
                item.hasExpense ? View.VISIBLE : View.INVISIBLE);

        holder.itemView.setOnClickListener(v -> listener.onClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class DayVH extends RecyclerView.ViewHolder {
        TextView dayText;
        View incomeIndicator;
        View expenseIndicator;

        DayVH(@NonNull View itemView) {
            super(itemView);
            dayText = itemView.findViewById(R.id.dayTextView);
            incomeIndicator = itemView.findViewById(R.id.incomeIndicator);
            expenseIndicator = itemView.findViewById(R.id.expenseIndicator);
        }
    }
}