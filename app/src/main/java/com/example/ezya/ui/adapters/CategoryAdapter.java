package com.example.ezya.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezya.R;
import com.example.ezya.data.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface OnCategoryDeleteListener {
        void onDelete(Category category);
    }

    public interface OnCategoryEditListener {
        void onEdit(Category category);
    }

    private List<Category> categoryList = new ArrayList<>();
    private OnCategoryDeleteListener deleteListener;
    private OnCategoryEditListener editListener;

    public void setCategoryList(List<Category> categoryList) {
        this.categoryList = categoryList;
        notifyDataSetChanged();
    }

    public void setDeleteListener(OnCategoryDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void setEditListener(OnCategoryEditListener editListener) {
        this.editListener = editListener;
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

        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDelete(category);
        });

        holder.itemView.setOnClickListener(v -> {
            if (editListener != null) editListener.onEdit(category);
        });
    }

    @Override
    public int getItemCount() { return categoryList.size(); }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView emojiTextView;
        TextView nameTextView;
        TextView amountTextView;
        TextView deleteButton;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            emojiTextView = itemView.findViewById(R.id.emojiTextView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            amountTextView = itemView.findViewById(R.id.amountTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}