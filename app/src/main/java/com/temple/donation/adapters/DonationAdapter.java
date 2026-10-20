package com.temple.donation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.temple.donation.R;
import com.temple.donation.model.Donation;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DonationAdapter extends RecyclerView.Adapter<DonationAdapter.DonationViewHolder> {

    private List<Donation> donations = new ArrayList<>();
    private final NumberFormat inrFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public void setDonations(List<Donation> donations) {
        this.donations = donations;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DonationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report_row, parent, false);
        return new DonationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DonationViewHolder holder, int position) {
        Donation d = donations.get(position);
        holder.tvTitle.setText(d.getDevoteeName() != null ? d.getDevoteeName() : ("Devotee #" + d.getDevoteeId()));
        holder.tvSubtitle.setText(d.getPaymentMode() + " • " + d.getDonationDate());
        holder.tvAmount.setText(inrFormat.format(d.getAmount()));
    }

    @Override
    public int getItemCount() {
        return donations.size();
    }

    static class DonationViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle, tvAmount;

        public DonationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvReportRowTitle);
            tvSubtitle = itemView.findViewById(R.id.tvReportRowSubtitle);
            tvAmount = itemView.findViewById(R.id.tvReportRowAmount);
        }
    }
}
