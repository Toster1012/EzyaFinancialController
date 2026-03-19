package com.example.ezya.ui.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ezya.R;
import com.example.ezya.data.model.Transaction;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DayTransactionAdapter extends
        RecyclerView.Adapter<DayTransactionAdapter.VH> {

    private List<Transaction> list = new ArrayList<>();

    public void setList(List<Transaction> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Transaction t = list.get(position);
        holder.emoji.setText(t.getCategoryEmoji());
        holder.name.setText(t.getCategoryName());

        if (t.getComment() != null && !t.getComment().isEmpty()) {
            holder.comment.setText(t.getComment());
            holder.comment.setVisibility(View.VISIBLE);
        } else {
            holder.comment.setVisibility(View.GONE);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        holder.date.setText(sdf.format(new Date(t.getTimestamp())));

        String sign = t.isExpense() ? "- " : "+ ";
        int color = t.isExpense() ? 0xFFFF5252 : 0xFF4CAF50;
        holder.amount.setText(String.format("%s%.0f ₽", sign, t.getAmount()));
        holder.amount.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView emoji, name, date, comment, amount;

        VH(@NonNull View itemView) {
            super(itemView);
            emoji = itemView.findViewById(R.id.transactionEmojiTextView);
            name = itemView.findViewById(R.id.transactionNameTextView);
            date = itemView.findViewById(R.id.transactionDateTextView);
            comment = itemView.findViewById(R.id.transactionCommentTextView);
            amount = itemView.findViewById(R.id.transactionAmountTextView);
        }
    }
}