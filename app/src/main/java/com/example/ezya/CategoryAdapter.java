package com.example.ezya;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categoryList = new ArrayList<>();

    public void setCategoryList(List<Category> categoryList) {
        this.categoryList = categoryList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.emojiTextView.setText(category.getEmoji());
        holder.nameTextView.setText(category.getName());
        holder.amountTextView.setText(String.format("%.0f ₽", category.getAmount()));
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView emojiTextView;
        TextView nameTextView;
        TextView amountTextView;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            emojiTextView = itemView.findViewById(R.id.emojiTextView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            amountTextView = itemView.findViewById(R.id.amountTextView);
        }
    }
}