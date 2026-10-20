package com.temple.donation.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;

import com.temple.donation.model.Devotee;
import com.temple.donation.model.Donation;
import com.temple.donation.model.Expense;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PdfReportGenerator {

    public static File generateMonthlyPdf(
            Context context,
            String templeName,
            String financialYear,
            String month,
            List<Donation> donations,
            double totalDonation,
            List<Expense> expenses,
            double totalExpense,
            int paidCount,
            int pendingCount
    ) {
        PdfDocument document = new PdfDocument();

        // Standard A4 Size: 595 x 842 points
        int pageWidth = 595;
        int pageHeight = 842;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();
        NumberFormat inr = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

        int margin = 40;
        int y = 50;

        // Header Background Banner (Temple Blue)
        paint.setColor(Color.rgb(21, 101, 192)); // #1565C0
        canvas.drawRect(0, 0, pageWidth, 110, paint);

        // Header Temple Name
        paint.setColor(Color.WHITE);
        paint.setTextSize(22);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(templeName, margin, 48, paint);

        // Subtitle: Month & Financial Year
        paint.setTextSize(14);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText("Monthly Financial Report: " + month + " (" + financialYear + ")", margin, 74, paint);

        // Generated Date
        String currentDate = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date());
        paint.setTextSize(10);
        paint.setColor(Color.rgb(227, 242, 253));
        canvas.drawText("Generated: " + currentDate, margin, 96, paint);

        y = 140;

        // Executive Summary Card
        paint.setColor(Color.rgb(241, 245, 249)); // Slate 100
        canvas.drawRect(margin, y, pageWidth - margin, y + 55, paint);

        paint.setColor(Color.rgb(15, 23, 42));
        paint.setTextSize(11);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Devotees Paid: " + paidCount, margin + 15, y + 25, paint);
        canvas.drawText("Devotees Pending: " + pendingCount, margin + 15, y + 42, paint);

        double balance = totalDonation - totalExpense;
        paint.setColor(Color.rgb(21, 101, 192));
        paint.setTextSize(13);
        canvas.drawText("Net Balance: " + inr.format(balance), margin + 260, y + 33, paint);

        y += 80;

        // ----------------------------------------------------
        // SECTION 1: DONATION DETAILS
        // ----------------------------------------------------
        paint.setColor(Color.rgb(21, 101, 192));
        paint.setTextSize(14);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("DONATION DETAILS", margin, y, paint);

        y += 18;
        // Table Header
        paint.setColor(Color.rgb(226, 232, 240));
        canvas.drawRect(margin, y - 12, pageWidth - margin, y + 8, paint);

        paint.setColor(Color.rgb(71, 85, 105));
        paint.setTextSize(10);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Devotee Name", margin + 10, y, paint);
        canvas.drawText("Mode", margin + 240, y, paint);
        canvas.drawText("Date", margin + 340, y, paint);
        canvas.drawText("Amount", pageWidth - margin - 80, y, paint);

        y += 20;

        paint.setColor(Color.rgb(15, 23, 42));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        if (donations.isEmpty()) {
            canvas.drawText("No donation records found for this month.", margin + 10, y, paint);
            y += 20;
        } else {
            for (Donation d : donations) {
                if (y > 700) break; // Avoid page overflow
                String name = d.getDevoteeName() != null ? d.getDevoteeName() : "Devotee #" + d.getDevoteeId();
                canvas.drawText(name, margin + 10, y, paint);
                canvas.drawText(d.getPaymentMode() != null ? d.getPaymentMode() : "Cash", margin + 240, y, paint);
                canvas.drawText(d.getDonationDate(), margin + 340, y, paint);
                canvas.drawText(inr.format(d.getAmount()), pageWidth - margin - 80, y, paint);

                // Divider line
                Paint linePaint = new Paint();
                linePaint.setColor(Color.rgb(241, 245, 249));
                canvas.drawLine(margin, y + 4, pageWidth - margin, y + 4, linePaint);

                y += 18;
            }
        }

        // Total Donation Row
        y += 5;
        paint.setColor(Color.rgb(22, 163, 74)); // Green
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(11);
        canvas.drawText("Total Donation: " + inr.format(totalDonation), pageWidth - margin - 170, y, paint);

        y += 35;

        // ----------------------------------------------------
        // SECTION 2: EXPENSE DETAILS
        // ----------------------------------------------------
        paint.setColor(Color.rgb(198, 40, 40)); // Red #C62828
        paint.setTextSize(14);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("EXPENSE DETAILS", margin, y, paint);

        y += 18;
        // Table Header
        paint.setColor(Color.rgb(226, 232, 240));
        canvas.drawRect(margin, y - 12, pageWidth - margin, y + 8, paint);

        paint.setColor(Color.rgb(71, 85, 105));
        paint.setTextSize(10);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Description", margin + 10, y, paint);
        canvas.drawText("Date", margin + 280, y, paint);
        canvas.drawText("Amount", pageWidth - margin - 80, y, paint);

        y += 20;

        paint.setColor(Color.rgb(15, 23, 42));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        if (expenses.isEmpty()) {
            canvas.drawText("No expense records found for this month.", margin + 10, y, paint);
            y += 20;
        } else {
            for (Expense exp : expenses) {
                if (y > 760) break;
                canvas.drawText(exp.getDescription(), margin + 10, y, paint);
                canvas.drawText(exp.getExpenseDate(), margin + 280, y, paint);
                canvas.drawText(inr.format(exp.getAmount()), pageWidth - margin - 80, y, paint);

                Paint linePaint = new Paint();
                linePaint.setColor(Color.rgb(241, 245, 249));
                canvas.drawLine(margin, y + 4, pageWidth - margin, y + 4, linePaint);

                y += 18;
            }
        }

        // Total Expense Row
        y += 5;
        paint.setColor(Color.rgb(198, 40, 40));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(11);
        canvas.drawText("Total Expense: " + inr.format(totalExpense), pageWidth - margin - 170, y, paint);

        y += 35;

        // ----------------------------------------------------
        // SUMMARY BOX
        // ----------------------------------------------------
        paint.setColor(Color.rgb(238, 242, 255)); // Indigo 50
        canvas.drawRect(margin, y, pageWidth - margin, y + 40, paint);

        paint.setColor(Color.rgb(30, 58, 138));
        paint.setTextSize(12);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        String balanceFormula = "Balance: " + inr.format(totalDonation) + " - " + inr.format(totalExpense) + " = " + inr.format(balance);
        canvas.drawText(balanceFormula, margin + 20, y + 25, paint);

        // Footer note
        paint.setColor(Color.rgb(148, 163, 184));
        paint.setTextSize(8);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText("Kanduah Satsang Vihar • Generated via Temple Donation Manager", margin, pageHeight - 20, paint);

        document.finishPage(page);

        // Save file to cache directory
        String fileName = "Temple_Report_" + month + "_" + financialYear.replace("-", "_") + ".pdf";
        File file = new File(context.getExternalCacheDir(), fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            document.close();
            return null;
        }
    }

    public static File generateYearlyPdf(
            Context context,
            String templeName,
            String financialYear,
            List<Devotee> devotees,
            List<Donation> donations,
            List<Expense> expenses
    ) {
        PdfDocument document = new PdfDocument();
        int pageWidth = 595;
        int pageHeight = 842;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();
        NumberFormat inr = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        int margin = 40;

        // Header
        paint.setColor(Color.rgb(21, 101, 192));
        canvas.drawRect(0, 0, pageWidth, 100, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(20);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(templeName, margin, 42, paint);

        paint.setTextSize(13);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText("Annual Financial Report: Financial Year " + financialYear + " (April - March)", margin, 68, paint);

        String currentDate = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date());
        paint.setTextSize(9);
        paint.setColor(Color.rgb(227, 242, 253));
        canvas.drawText("Generated: " + currentDate, margin, 88, paint);

        int y = 130;
        String[] monthsList = {"April", "May", "June", "July", "August", "September", "October", "November", "December", "January", "February", "March"};

        // Table Header
        paint.setColor(Color.rgb(226, 232, 240));
        canvas.drawRect(margin, y - 12, pageWidth - margin, y + 8, paint);

        paint.setColor(Color.rgb(71, 85, 105));
        paint.setTextSize(10);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Month", margin + 10, y, paint);
        canvas.drawText("Donations", margin + 140, y, paint);
        canvas.drawText("Expenses", margin + 260, y, paint);
        canvas.drawText("Net Balance", margin + 380, y, paint);

        y += 20;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(10);

        double grandDonations = 0;
        double grandExpenses = 0;

        for (String m : monthsList) {
            double donTotal = 0;
            for (Donation d : donations) {
                if (financialYear.equals(d.getFinancialYear()) && m.equalsIgnoreCase(d.getMonth())) {
                    donTotal += d.getAmount();
                }
            }

            double expTotal = 0;
            for (Expense e : expenses) {
                if (financialYear.equals(e.getFinancialYear()) && m.equalsIgnoreCase(e.getMonth())) {
                    expTotal += e.getAmount();
                }
            }

            grandDonations += donTotal;
            grandExpenses += expTotal;
            double monthBal = donTotal - expTotal;

            paint.setColor(Color.rgb(15, 23, 42));
            canvas.drawText(m, margin + 10, y, paint);

            paint.setColor(Color.rgb(22, 163, 74));
            canvas.drawText(inr.format(donTotal), margin + 140, y, paint);

            paint.setColor(Color.rgb(198, 40, 40));
            canvas.drawText(inr.format(expTotal), margin + 260, y, paint);

            paint.setColor(monthBal >= 0 ? Color.rgb(21, 101, 192) : Color.rgb(198, 40, 40));
            canvas.drawText(inr.format(monthBal), margin + 380, y, paint);

            Paint linePaint = new Paint();
            linePaint.setColor(Color.rgb(241, 245, 249));
            canvas.drawLine(margin, y + 4, pageWidth - margin, y + 4, linePaint);

            y += 18;
        }

        // Grand Totals Row
        y += 10;
        paint.setColor(Color.rgb(238, 242, 255));
        canvas.drawRect(margin, y - 12, pageWidth - margin, y + 16, paint);

        paint.setColor(Color.rgb(15, 23, 42));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(11);
        canvas.drawText("GRAND TOTAL", margin + 10, y + 2, paint);

        paint.setColor(Color.rgb(22, 163, 74));
        canvas.drawText(inr.format(grandDonations), margin + 140, y + 2, paint);

        paint.setColor(Color.rgb(198, 40, 40));
        canvas.drawText(inr.format(grandExpenses), margin + 260, y + 2, paint);

        double grandBal = grandDonations - grandExpenses;
        paint.setColor(Color.rgb(21, 101, 192));
        canvas.drawText(inr.format(grandBal), margin + 380, y + 2, paint);

        document.finishPage(page);

        String fileName = "Temple_Annual_Report_" + financialYear.replace("-", "_") + ".pdf";
        File file = new File(context.getExternalCacheDir(), fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            document.close();
            return null;
        }
    }

    public static File generateDevoteesMasterPdf(
            Context context,
            String templeName,
            List<Devotee> devotees,
            List<Donation> donations
    ) {
        PdfDocument document = new PdfDocument();
        int pageWidth = 595;
        int pageHeight = 842;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();
        int margin = 40;

        paint.setColor(Color.rgb(21, 101, 192));
        canvas.drawRect(0, 0, pageWidth, 90, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(20);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(templeName, margin, 40, paint);

        paint.setTextSize(13);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText("Master Devotee Directory (" + devotees.size() + " Devotees)", margin, 64, paint);

        int y = 120;
        paint.setColor(Color.rgb(226, 232, 240));
        canvas.drawRect(margin, y - 12, pageWidth - margin, y + 8, paint);

        paint.setColor(Color.rgb(71, 85, 105));
        paint.setTextSize(10);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Name", margin + 10, y, paint);
        canvas.drawText("Mobile", margin + 160, y, paint);
        canvas.drawText("Address", margin + 260, y, paint);
        canvas.drawText("Default (INR)", margin + 370, y, paint);
        canvas.drawText("Status", pageWidth - margin - 45, y, paint);

        y += 20;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(9);
        paint.setColor(Color.rgb(15, 23, 42));

        for (Devotee d : devotees) {
            if (y > 780) break;
            canvas.drawText(d.getName(), margin + 10, y, paint);
            canvas.drawText(d.getMobile() != null ? d.getMobile() : "-", margin + 160, y, paint);
            canvas.drawText(d.getAddress() != null ? d.getAddress() : "-", margin + 260, y, paint);
            canvas.drawText("₹" + (int) d.getMonthlyAmount() + "/mo", margin + 370, y, paint);
            canvas.drawText(d.isActive() ? "Active" : "Inactive", pageWidth - margin - 45, y, paint);

            Paint linePaint = new Paint();
            linePaint.setColor(Color.rgb(241, 245, 249));
            canvas.drawLine(margin, y + 4, pageWidth - margin, y + 4, linePaint);
            y += 18;
        }

        document.finishPage(page);

        File file = new File(context.getExternalCacheDir(), "Temple_Devotees_Master.pdf");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            document.close();
            return null;
        }
    }

    public static File generateDonationsMasterPdf(
            Context context,
            String templeName,
            List<Donation> donations,
            List<Devotee> devotees
    ) {
        PdfDocument document = new PdfDocument();
        int pageWidth = 595;
        int pageHeight = 842;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();
        int margin = 40;

        paint.setColor(Color.rgb(21, 101, 192));
        canvas.drawRect(0, 0, pageWidth, 90, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(20);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(templeName, margin, 40, paint);

        paint.setTextSize(13);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText("Master Donations Ledger (" + donations.size() + " Records)", margin, 64, paint);

        int y = 120;
        paint.setColor(Color.rgb(226, 232, 240));
        canvas.drawRect(margin, y - 12, pageWidth - margin, y + 8, paint);

        paint.setColor(Color.rgb(71, 85, 105));
        paint.setTextSize(10);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Devotee Name", margin + 10, y, paint);
        canvas.drawText("FY", margin + 180, y, paint);
        canvas.drawText("Month", margin + 240, y, paint);
        canvas.drawText("Date", margin + 320, y, paint);
        canvas.drawText("Amount", pageWidth - margin - 50, y, paint);

        y += 20;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(9);
        paint.setColor(Color.rgb(15, 23, 42));

        for (Donation d : donations) {
            if (y > 780) break;
            String name = d.getDevoteeName() != null ? d.getDevoteeName() : "Devotee #" + d.getDevoteeId();
            canvas.drawText(name, margin + 10, y, paint);
            canvas.drawText(d.getFinancialYear(), margin + 180, y, paint);
            canvas.drawText(d.getMonth(), margin + 240, y, paint);
            canvas.drawText(d.getDonationDate(), margin + 320, y, paint);
            canvas.drawText("₹" + (int) d.getAmount(), pageWidth - margin - 50, y, paint);

            Paint linePaint = new Paint();
            linePaint.setColor(Color.rgb(241, 245, 249));
            canvas.drawLine(margin, y + 4, pageWidth - margin, y + 4, linePaint);
            y += 18;
        }

        document.finishPage(page);

        File file = new File(context.getExternalCacheDir(), "Temple_Donations_Master_Ledger.pdf");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            document.close();
            return null;
        }
    }

    public static File generateExpensesMasterPdf(
            Context context,
            String templeName,
            List<Expense> expenses
    ) {
        PdfDocument document = new PdfDocument();
        int pageWidth = 595;
        int pageHeight = 842;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();
        int margin = 40;

        paint.setColor(Color.rgb(21, 101, 192));
        canvas.drawRect(0, 0, pageWidth, 90, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(20);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(templeName, margin, 40, paint);

        paint.setTextSize(13);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        canvas.drawText("Master Expenses Ledger (" + expenses.size() + " Records)", margin, 64, paint);

        int y = 120;
        paint.setColor(Color.rgb(226, 232, 240));
        canvas.drawRect(margin, y - 12, pageWidth - margin, y + 8, paint);

        paint.setColor(Color.rgb(71, 85, 105));
        paint.setTextSize(10);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Description", margin + 10, y, paint);
        canvas.drawText("FY", margin + 200, y, paint);
        canvas.drawText("Month", margin + 260, y, paint);
        canvas.drawText("Date", margin + 340, y, paint);
        canvas.drawText("Amount", pageWidth - margin - 50, y, paint);

        y += 20;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(9);
        paint.setColor(Color.rgb(15, 23, 42));

        for (Expense e : expenses) {
            if (y > 780) break;
            canvas.drawText(e.getDescription(), margin + 10, y, paint);
            canvas.drawText(e.getFinancialYear(), margin + 200, y, paint);
            canvas.drawText(e.getMonth(), margin + 260, y, paint);
            canvas.drawText(e.getExpenseDate(), margin + 340, y, paint);
            canvas.drawText("₹" + (int) e.getAmount(), pageWidth - margin - 50, y, paint);

            Paint linePaint = new Paint();
            linePaint.setColor(Color.rgb(241, 245, 249));
            canvas.drawLine(margin, y + 4, pageWidth - margin, y + 4, linePaint);
            y += 18;
        }

        document.finishPage(page);

        File file = new File(context.getExternalCacheDir(), "Temple_Expenses_Master_Ledger.pdf");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            document.close();
            return null;
        }
    }
}
