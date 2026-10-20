package com.temple.donation.utils;

import android.content.Context;

import com.temple.donation.model.Devotee;
import com.temple.donation.model.Donation;
import com.temple.donation.model.Expense;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExcelReportGenerator {

    private static final String[] MONTHS = {
            "April", "May", "June", "July", "August", "September",
            "October", "November", "December", "January", "February", "March"
    };

    public static File generateMonthlyExcel(
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
        String fileName = "Temple_Report_" + month + "_" + financialYear.replace("-", "_") + ".csv";
        File file = new File(context.getExternalCacheDir(), fileName);

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write("\uFEFF");
            writer.write("\"" + templeName + "\"\n");
            writer.write("\"Monthly Financial Statement: " + month + " (" + financialYear + ")\"\n");
            writer.write("\"Generated: " + new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date()) + "\"\n\n");

            writer.write("\"KEY METRICS\",\n");
            writer.write("\"Devotees Paid\"," + paidCount + "\n");
            writer.write("\"Devotees Pending\"," + pendingCount + "\n");
            writer.write("\"Total Donations (INR)\"," + totalDonation + "\n");
            writer.write("\"Total Expenses (INR)\"," + totalExpense + "\n");
            writer.write("\"Net Balance (INR)\"," + (totalDonation - totalExpense) + "\n\n");

            writer.write("\"DONATION DETAILS\",\n");
            writer.write("\"Sl No.\",\"Devotee Name\",\"Payment Mode\",\"Donation Date\",\"Amount (INR)\",\"Remarks\"\n");
            for (int i = 0; i < donations.size(); i++) {
                Donation d = donations.get(i);
                String name = d.getDevoteeName() != null ? d.getDevoteeName() : "Devotee #" + d.getDevoteeId();
                writer.write((i + 1) + ",\"" + escapeCsv(name) + "\",\"" + escapeCsv(d.getPaymentMode()) + "\",\""
                        + escapeCsv(d.getDonationDate()) + "\"," + d.getAmount() + ",\"" + escapeCsv(d.getRemarks()) + "\"\n");
            }
            writer.write(",,,\"Total Donations:\"," + totalDonation + ",\n\n");

            writer.write("\"EXPENSE DETAILS\",\n");
            writer.write("\"Sl No.\",\"Description\",\"Expense Date\",\"Amount (INR)\",\"Remarks\"\n");
            for (int i = 0; i < expenses.size(); i++) {
                Expense e = expenses.get(i);
                writer.write((i + 1) + ",\"" + escapeCsv(e.getDescription()) + "\",\"" + escapeCsv(e.getExpenseDate()) + "\","
                        + e.getAmount() + ",\"" + escapeCsv(e.getRemarks()) + "\"\n");
            }
            writer.write(",,\"Total Expenses:\"," + totalExpense + ",\n\n");

            writer.write("\"SUMMARY BALANCE\",\n");
            writer.write("\"Net Surplus / Deficit\"," + (totalDonation - totalExpense) + "\n");

            writer.flush();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File generateYearlyExcel(
            Context context,
            String templeName,
            String financialYear,
            List<Devotee> devotees,
            List<Donation> donations,
            List<Expense> expenses
    ) {
        String fileName = "Temple_Annual_Report_" + financialYear.replace("-", "_") + ".csv";
        File file = new File(context.getExternalCacheDir(), fileName);

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write("\uFEFF");
            writer.write("\"" + templeName + "\"\n");
            writer.write("\"Annual Financial Report for Financial Year " + financialYear + " (April - March)\"\n");
            writer.write("\"Generated: " + new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date()) + "\"\n\n");

            writer.write("\"12-MONTH FINANCIAL PERFORMANCE (APRIL - MARCH)\",\n");
            writer.write("\"Month\",\"Total Donations (INR)\",\"Total Expenses (INR)\",\"Net Balance (INR)\",\"Devotees Paid\"\n");

            double grandDonation = 0;
            double grandExpense = 0;

            for (String m : MONTHS) {
                double donTotal = 0;
                int paidCount = 0;
                for (Donation d : donations) {
                    if (financialYear.equals(d.getFinancialYear()) && m.equalsIgnoreCase(d.getMonth())) {
                        donTotal += d.getAmount();
                        paidCount++;
                    }
                }

                double expTotal = 0;
                for (Expense e : expenses) {
                    if (financialYear.equals(e.getFinancialYear()) && m.equalsIgnoreCase(e.getMonth())) {
                        expTotal += e.getAmount();
                    }
                }

                grandDonation += donTotal;
                grandExpense += expTotal;

                writer.write("\"" + m + "\"," + donTotal + "," + expTotal + "," + (donTotal - expTotal) + "," + paidCount + "\n");
            }

            writer.write("\"GRAND TOTAL\"," + grandDonation + "," + grandExpense + "," + (grandDonation - grandExpense) + ",\n\n");

            writer.write("\"DEVOTEE-WISE 12-MONTH CONTRIBUTION MATRIX\",\n");
            StringBuilder headers = new StringBuilder("\"Devotee Name\",\"Mobile\",\"Default (INR)\"");
            for (String m : MONTHS) {
                headers.append(",\"").append(m).append("\"");
            }
            headers.append(",\"Annual Total (INR)\"\n");
            writer.write(headers.toString());

            for (Devotee dev : devotees) {
                StringBuilder row = new StringBuilder("\"" + escapeCsv(dev.getName()) + "\",\"" + escapeCsv(dev.getMobile()) + "\"," + dev.getMonthlyAmount());
                double devAnnualTotal = 0;
                for (String m : MONTHS) {
                    Donation matching = null;
                    for (Donation d : donations) {
                        if (d.getDevoteeId() == dev.getId() && financialYear.equals(d.getFinancialYear()) && m.equalsIgnoreCase(d.getMonth())) {
                            matching = d;
                            break;
                        }
                    }
                    if (matching != null) {
                        devAnnualTotal += matching.getAmount();
                        row.append(",").append(matching.getAmount());
                    } else {
                        row.append(",\"-\"");
                    }
                }
                row.append(",").append(devAnnualTotal).append("\n");
                writer.write(row.toString());
            }

            writer.flush();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File generateDevoteesMasterExcel(
            Context context,
            String templeName,
            List<Devotee> devotees,
            List<Donation> donations
    ) {
        String fileName = "Temple_Devotees_Master.csv";
        File file = new File(context.getExternalCacheDir(), fileName);

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write("\uFEFF");
            writer.write("\"" + templeName + "\"\n");
            writer.write("\"Master Devotee Directory\"\n");
            writer.write("\"Generated: " + new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date()) + "\"\n\n");

            writer.write("\"ID\",\"Devotee Name\",\"Mobile Number\",\"Address\",\"Default Monthly Amount (INR)\",\"Status\",\"Lifetime Donations (INR)\"\n");

            for (Devotee d : devotees) {
                double lifetime = 0;
                for (Donation don : donations) {
                    if (don.getDevoteeId() == d.getId()) {
                        lifetime += don.getAmount();
                    }
                }
                writer.write(d.getId() + ",\"" + escapeCsv(d.getName()) + "\",\"" + escapeCsv(d.getMobile()) + "\",\""
                        + escapeCsv(d.getAddress()) + "\"," + d.getMonthlyAmount() + ",\""
                        + (d.isActive() ? "Active" : "Inactive") + "\"," + lifetime + "\n");
            }

            writer.flush();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File generateDonationsMasterExcel(
            Context context,
            String templeName,
            List<Donation> donations,
            List<Devotee> devotees
    ) {
        String fileName = "Temple_Donations_Master_Ledger.csv";
        File file = new File(context.getExternalCacheDir(), fileName);

        Map<Long, String> nameMap = new HashMap<>();
        for (Devotee d : devotees) {
            nameMap.put(d.getId(), d.getName());
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write("\uFEFF");
            writer.write("\"" + templeName + "\"\n");
            writer.write("\"Master Donations Ledger\"\n");
            writer.write("\"Generated: " + new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date()) + "\"\n\n");

            writer.write("\"ID\",\"Devotee Name\",\"Financial Year\",\"Month\",\"Amount (INR)\",\"Donation Date\",\"Payment Mode\",\"Remarks\"\n");

            double total = 0;
            for (Donation d : donations) {
                String devName = nameMap.containsKey(d.getDevoteeId()) ? nameMap.get(d.getDevoteeId()) : "Devotee #" + d.getDevoteeId();
                total += d.getAmount();
                writer.write(d.getId() + ",\"" + escapeCsv(devName) + "\",\"" + escapeCsv(d.getFinancialYear()) + "\",\""
                        + escapeCsv(d.getMonth()) + "\"," + d.getAmount() + ",\"" + escapeCsv(d.getDonationDate()) + "\",\""
                        + escapeCsv(d.getPaymentMode()) + "\",\"" + escapeCsv(d.getRemarks()) + "\"\n");
            }
            writer.write(",,,,\"Total Donations:\"," + total + ",,\n");

            writer.flush();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File generateExpensesMasterExcel(
            Context context,
            String templeName,
            List<Expense> expenses
    ) {
        String fileName = "Temple_Expenses_Master_Ledger.csv";
        File file = new File(context.getExternalCacheDir(), fileName);

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write("\uFEFF");
            writer.write("\"" + templeName + "\"\n");
            writer.write("\"Master Expenses Ledger\"\n");
            writer.write("\"Generated: " + new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date()) + "\"\n\n");

            writer.write("\"ID\",\"Financial Year\",\"Month\",\"Description\",\"Amount (INR)\",\"Expense Date\",\"Remarks\"\n");

            double total = 0;
            for (Expense e : expenses) {
                total += e.getAmount();
                writer.write(e.getId() + ",\"" + escapeCsv(e.getFinancialYear()) + "\",\"" + escapeCsv(e.getMonth()) + "\",\""
                        + escapeCsv(e.getDescription()) + "\"," + e.getAmount() + ",\"" + escapeCsv(e.getExpenseDate()) + "\",\""
                        + escapeCsv(e.getRemarks()) + "\"\n");
            }
            writer.write(",,,,\"Total Expenses:\"," + total + ",\n");

            writer.flush();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}
