package com.example.ezya.ui.adapters;

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

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList = new ArrayList<>();
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());

    public void setTransactionList(List<Transaction> transactionList) {
        this.transactionList = transactionList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        holder.emojiTextView.setText(transaction.getCategoryEmoji());
        holder.nameTextView.setText(transaction.getCategoryName());
        holder.dateTextView.setText(dateFormat.format(new Date(transaction.getTimestamp())));

        String comment = transaction.getComment();
        if (comment != null && !comment.isEmpty()) {
            holder.commentTextView.setVisibility(View.VISIBLE);
            holder.commentTextView.setText(comment);
        } else {
            holder.commentTextView.setVisibility(View.GONE);
        }

        if (transaction.isExpense()) {
            holder.amountTextView.setText(String.format("-%.0f ₽", transaction.getAmount()));
            holder.amountTextView.setTextColor(0xFFFF5252);
        } else {
            holder.amountTextView.setText(String.format("+%.0f ₽", transaction.getAmount()));
            holder.amountTextView.setTextColor(0xFF4CAF50);
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView emojiTextView;
        TextView nameTextView;
        TextView amountTextView;
        TextView dateTextView;
        TextView commentTextView;

        TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            emojiTextView = itemView.findViewById(R.id.transactionEmojiTextView);
            nameTextView = itemView.findViewById(R.id.transactionNameTextView);
            amountTextView = itemView.findViewById(R.id.transactionAmountTextView);
            dateTextView = itemView.findViewById(R.id.transactionDateTextView);
            commentTextView = itemView.findViewById(R.id.transactionCommentTextView);
        }
    }
}