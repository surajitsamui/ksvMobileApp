package com.temple.donation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.temple.donation.R;
import com.temple.donation.model.Expense;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    public interface OnExpenseDeleteListener {
        void onDelete(Expense expense);
    }

    private List<Expense> expenses = new ArrayList<>();
    private final OnExpenseDeleteListener deleteListener;
    private final NumberFormat inrFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public ExpenseAdapter(OnExpenseDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.tvDesc.setText(expense.getDescription());
        holder.tvAmount.setText(inrFormat.format(expense.getAmount()));
        holder.tvDate.setText(expense.getExpenseDate());

        if (expense.getRemarks() != null && !expense.getRemarks().trim().isEmpty()) {
            holder.tvRemarks.setVisibility(View.VISIBLE);
            holder.tvRemarks.setText(expense.getRemarks());
        } else {
            holder.tvRemarks.setVisibility(View.GONE);
        }

        if (deleteListener != null) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> deleteListener.onDelete(expense));
        } else {
            holder.btnDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvDesc, tvAmount, tvDate, tvRemarks;
        ImageButton btnDelete;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDesc = itemView.findViewById(R.id.tvExpenseItemDesc);
            tvAmount = itemView.findViewById(R.id.tvExpenseItemAmount);
            tvDate = itemView.findViewById(R.id.tvExpenseItemDate);
            tvRemarks = itemView.findViewById(R.id.tvExpenseItemRemarks);
            btnDelete = itemView.findViewById(R.id.btnExpenseItemDelete);
        }
    }
}
