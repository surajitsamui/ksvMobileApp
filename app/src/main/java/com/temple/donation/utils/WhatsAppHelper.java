
package com.temple.donation.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.temple.donation.model.Devotee;

import java.util.Locale;

public final class WhatsAppHelper {

    private WhatsAppHelper() {
        // Utility class
    }

    /**
     * Opens WhatsApp with a personalized message.
     *
     * @param context       Activity or application context
     * @param devotee       Selected devotee
     * @param financialYear Selected financial year
     * @param month         Selected month
     * @param isPaid        True for payment confirmation,
     *                      false for contribution reminder
     */
    public static void sendContributionMessage(
            Context context,
            Devotee devotee,
            String financialYear,
            String month,
            boolean isPaid) {

        if (devotee == null) {
            Toast.makeText(context,
                    "Please select a devotee",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String name = devotee.getName();
        String mobile = devotee.getMobile();

        if (mobile == null || mobile.trim().isEmpty()) {
            Toast.makeText(context,
                    "Please add the devotee's mobile number",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Remove spaces, +, hyphens and other formatting.
        String phone = mobile.replaceAll("[^0-9]", "");

        // Convert Indian numbers to international format.
        if (phone.length() == 10) {
            phone = "91" + phone;
        } else if (phone.length() == 11 && phone.startsWith("0")) {
            phone = "91" + phone.substring(1);
        }

        if (phone.length() < 11 || phone.length() > 15) {
            Toast.makeText(context,
                    "Please check the mobile number and country code",
                    Toast.LENGTH_LONG).show();
            return;
        }

        double amount = devotee.getMonthlyAmount();

        String formattedAmount =
                String.format(Locale.US, "%.2f", amount);

        String message;

        if (isPaid) {
            message =
                    "Namaskar " + name + ",\n\n" +
                            "Your monthly contribution has been recorded " +
                            "successfully by Kanduah Satsang Vihar.\n\n" +
                            "Financial Year: " + financialYear + "\n" +
                            "Month: " + month + "\n" +
                            "Amount Received: Rs. " + formattedAmount + "\n\n" +
                            "Thank you for your valuable support.\n\n" +
                            "Regards,\nKanduah Satsang Vihar";
        } else {
            message =
                    "Namaskar " + name + ",\n\n" +
                            "This is a gentle reminder regarding your monthly " +
                            "contribution to Kanduah Satsang Vihar.\n\n" +
                            "Financial Year: " + financialYear + "\n" +
                            "Month: " + month + "\n" +
                            "Suggested Contribution: Rs. " +
                            formattedAmount + "\n\n" +
                            "If you have already contributed, please ignore " +
                            "this reminder.\n\n" +
                            "Thank you for your kind support.\n\n" +
                            "Regards,\nKanduah Satsang Vihar";
        }

        String url = "https://wa.me/" + phone
                + "?text=" + Uri.encode(message);

        try {
            Intent intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
            );

            context.startActivity(intent);

        } catch (ActivityNotFoundException e) {
            Toast.makeText(context,
                    "Unable to open WhatsApp. Please check its installation.",
                    Toast.LENGTH_LONG).show();
        }
    }
}
