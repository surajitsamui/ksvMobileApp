
package com.temple.donation.adapters;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.temple.donation.R;
import com.temple.donation.model.Devotee;
import com.temple.donation.model.Donation;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MonthlyDonationAdapter
        extends RecyclerView.Adapter<MonthlyDonationAdapter.MonthlyViewHolder> {

    public interface OnMonthClickListener {
        void onMonthSelected(String monthName);
    }

    public static class MonthItem {
        public String monthName;
        public double amount;
        public boolean isPaid;
        public String paymentMode;
        public String date;

        public MonthItem(String monthName, double amount,
                         boolean isPaid, String paymentMode, String date) {
            this.monthName = monthName;
            this.amount = amount;
            this.isPaid = isPaid;
            this.paymentMode = paymentMode;
            this.date = date;
        }
    }

    private final List<MonthItem> monthItems = new ArrayList<>();
    private final OnMonthClickListener listener;

    private final NumberFormat inrFormat =
            NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    private Devotee selectedDevotee;
    private String financialYear = "";

    public MonthlyDonationAdapter(OnMonthClickListener listener) {
        this.listener = listener;
    }

    public void setMessageDetails(Devotee devotee, String financialYear) {
        this.selectedDevotee = devotee;
        this.financialYear =
                financialYear == null ? "" : financialYear;
    }



    public void updateData(
            String[] months,
            List<Donation> donations,
            double defaultAmount) {

        monthItems.clear();

        Map<String, Donation> donationMap = new HashMap<>();

        if (donations != null) {
            android.util.Log.d("DonationDebug",
                    "Total donation records: " + donations.size());

            for (Donation donation : donations) {
                if (donation == null || donation.getMonth() == null) {
                    continue;
                }

                String originalMonth = donation.getMonth();
                String key = originalMonth.trim()
                        .toLowerCase(Locale.ROOT);

                android.util.Log.d("DonationDebug",
                        "Database month: [" + originalMonth
                                + "], normalized: [" + key + "]");

                donationMap.put(key, donation);
            }
        }

        for (String month : months) {
            String key = month.trim().toLowerCase(Locale.ROOT);
            Donation donation = donationMap.get(key);

            android.util.Log.d("DonationDebug",
                    "Display month: " + month
                            + ", matched: " + (donation != null));

            if (donation != null) {
                monthItems.add(new MonthItem(
                        month,
                        donation.getAmount(),
                        true,
                        donation.getPaymentMode(),
                        donation.getDonationDate()
                ));
            } else {
                monthItems.add(new MonthItem(
                        month,
                        defaultAmount,
                        false,
                        null,
                        null
                ));
            }
        }
        android.util.Log.d("DonationDebug",
                "Months received: " + months.length
                        + ", Adapter items: " + monthItems.size());
        notifyDataSetChanged();
    }


//    public void updateData(String[] months,
//                           List<Donation> donations,
//                           double defaultAmount) {
//
//        monthItems.clear();
//
//        Map<String, Donation> donationMap = new HashMap<>();
//
//        if (donations != null) {
//            for (Donation donation : donations) {
//                if (donation.getMonth() != null) {
//                    donationMap.put(
//                            donation.getMonth().toLowerCase(Locale.ROOT),
//                            donation
//                    );
//                }
//            }
//        }
//
//        for (String month : months) {
//            Donation donation =
//                    donationMap.get(month.toLowerCase(Locale.ROOT));
//
//            if (donation != null) {
//                monthItems.add(new MonthItem(
//                        month,
//                        donation.getAmount(),
//                        true,
//                        donation.getPaymentMode(),
//                        donation.getDonationDate()
//                ));
//            } else {
//                monthItems.add(new MonthItem(
//                        month, defaultAmount, false, null, null
//                ));
//            }
//        }
//
//        notifyDataSetChanged();
//    }

    @NonNull
    @Override
    public MonthlyViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_monthly_status, parent, false);

        return new MonthlyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull MonthlyViewHolder holder, int position) {

        MonthItem item = monthItems.get(position);

        holder.tvMonth.setText(item.monthName);
        holder.tvAmount.setText(inrFormat.format(item.amount));

        if (item.isPaid) {
            holder.tvStatus.setText("Paid");
            holder.tvStatus.setTextColor(Color.parseColor("#16A34A"));
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_paid);
            holder.btnAction.setText("Edit");
        } else {
            holder.tvStatus.setText("Pending");
            holder.tvStatus.setTextColor(Color.parseColor("#EA580C"));
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
            holder.btnAction.setText("Pay");
        }

        // Existing Pay/Edit functionality
        holder.btnAction.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMonthSelected(item.monthName);
            }
        });

        // Send an individual SMS for this month
        holder.btnSendSms.setOnClickListener(v ->
                sendMonthlyStatusSms(v, item));
    }

    private void sendMonthlyStatusSms(View view, MonthItem item) {

        if (selectedDevotee == null) {
            Toast.makeText(view.getContext(),
                    "Please select a devotee first",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String mobile = selectedDevotee.getMobile();

        if (mobile == null || mobile.trim().isEmpty()) {
            Toast.makeText(view.getContext(),
                    "Please add the devotee's mobile number",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Preserve a leading + and remove spaces and punctuation.
        mobile = mobile.trim().replaceAll("[^0-9+]", "");

        String name = selectedDevotee.getName();
        String message;

        if (item.isPaid) {
            message = "জয়গুরু 🙏 " + name + ",\n\n"
                    + "কান্দুয়া সৎসঙ্গ বিহারে আপনার মাসিক অর্ঘ্য "
                    + "সফলভাবে জমা হয়েছে।\n\n"
                    + "আর্থিক বর্ষ: " + financialYear + "\n"
                    + "মাস: " + item.monthName + "\n"
                    + "জমাকৃত অর্ঘ্যের পরিমাণ: "
                    + inrFormat.format(item.amount) + "\n\n"
                    + "আপনার মূল্যবান সহযোগিতার জন্য "
                    + "আন্তরিক ধন্যবাদ।\n\n"
                    + "জয়গুরু 🙏\n"
                    + "কান্দুয়া সৎসঙ্গ বিহার";
        } else {
            message = "জয়গুরু 🙏 " + name + ",\n\n"
                    + "কান্দুয়া সৎসঙ্গ বিহারের পক্ষ থেকে আপনার "
                    + "মাসিক অর্ঘ্য প্রদানের জন্য বিনীতভাবে "
                    + "স্মরণ করিয়ে দেওয়া হচ্ছে।\n\n"
                    + "আর্থিক বর্ষ: " + financialYear + "\n"
                    + "মাস: " + item.monthName + "\n"
                    + "প্রস্তাবিত অর্ঘ্যের পরিমাণ: "
                    + inrFormat.format(item.amount) + "\n\n"
                    + "আপনি যদি ইতিমধ্যে অর্ঘ্য প্রদান করে থাকেন, "
                    + "তাহলে এই বার্তাটি উপেক্ষা করবেন।\n\n"
                    + "আপনার মূল্যবান সহযোগিতার জন্য "
                    + "আন্তরিক ধন্যবাদ।\n\n"
                    + "শুভেচ্ছান্তে,\n"
                    + "কান্দুয়া সৎসঙ্গ বিহার";
        }

        Intent intent = new Intent(
                Intent.ACTION_SENDTO,
                Uri.parse("smsto:" + mobile)
        );

        intent.putExtra("sms_body", message);

        try {
            view.getContext().startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(view.getContext(),
                    "No SMS application found",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public int getItemCount() {
        return monthItems.size();
    }

    static class MonthlyViewHolder extends RecyclerView.ViewHolder {

        TextView tvMonth;
        TextView tvAmount;
        TextView tvStatus;
        Button btnAction;
        Button btnSendSms;

        MonthlyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvMonth = itemView.findViewById(R.id.tvMonthName);
            tvAmount = itemView.findViewById(R.id.tvMonthAmount);
            tvStatus = itemView.findViewById(R.id.tvMonthStatus);
            btnAction = itemView.findViewById(R.id.btnMonthAction);
            btnSendSms = itemView.findViewById(R.id.btnSendSms);
        }
    }
}
