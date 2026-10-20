package com.temple.donation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.temple.donation.R;
import com.temple.donation.model.Devotee;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DevoteeAdapter extends RecyclerView.Adapter<DevoteeAdapter.DevoteeViewHolder> {

    public interface OnDevoteeActionListener {
        void onEdit(Devotee devotee);
        void onToggleStatus(Devotee devotee);
        void onViewDonations(Devotee devotee);
        void onDelete(Devotee devotee);
    }

    private List<Devotee> devotees = new ArrayList<>();
    private final OnDevoteeActionListener listener;
    private final NumberFormat inrFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public DevoteeAdapter(OnDevoteeActionListener listener) {
        this.listener = listener;
    }

    public void setDevotees(List<Devotee> devotees) {
        this.devotees = devotees;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DevoteeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_devotee, parent, false);
        return new DevoteeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DevoteeViewHolder holder, int position) {
        Devotee devotee = devotees.get(position);
        holder.tvName.setText(devotee.getName());
        holder.tvMobile.setText("Mobile: " + (devotee.getMobile() != null ? devotee.getMobile() : "N/A"));
        holder.tvAddress.setText("Address: " + (devotee.getAddress() != null ? devotee.getAddress() : "N/A"));
        holder.tvDefaultAmount.setText("Default: " + inrFormat.format(devotee.getMonthlyAmount()) + "/mo");

        if (devotee.isActive()) {
            holder.tvStatus.setText("Active");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_paid);
            holder.btnToggleStatus.setText("Deactivate");
        } else {
            holder.tvStatus.setText("Inactive");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
            holder.btnToggleStatus.setText("Activate");
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(devotee));
        holder.btnToggleStatus.setOnClickListener(v -> listener.onToggleStatus(devotee));
        holder.btnDonations.setOnClickListener(v -> listener.onViewDonations(devotee));
        if (holder.btnDelete != null) {
            holder.btnDelete.setOnClickListener(v -> listener.onDelete(devotee));
        }
    }

    @Override
    public int getItemCount() {
        return devotees.size();
    }

    static class DevoteeViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvMobile, tvAddress, tvDefaultAmount, tvStatus;
        Button btnEdit, btnToggleStatus, btnDonations, btnDelete;

        public DevoteeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvItemDevoteeName);
            tvMobile = itemView.findViewById(R.id.tvItemDevoteeMobile);
            tvAddress = itemView.findViewById(R.id.tvItemDevoteeAddress);
            tvDefaultAmount = itemView.findViewById(R.id.tvItemDevoteeAmount);
            tvStatus = itemView.findViewById(R.id.tvItemDevoteeStatus);
            btnEdit = itemView.findViewById(R.id.btnItemDevoteeEdit);
            btnToggleStatus = itemView.findViewById(R.id.btnItemDevoteeToggle);
            btnDonations = itemView.findViewById(R.id.btnItemDevoteeDonations);
            btnDelete = itemView.findViewById(R.id.btnItemDevoteeDelete);
        }
    }
}
