package com.temple.donation.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.temple.donation.R;
import com.temple.donation.adapters.DonationAdapter;
import com.temple.donation.adapters.ExpenseAdapter;
import com.temple.donation.network.BackendApi;

import org.json.JSONArray;
import org.json.JSONObject;
import com.temple.donation.model.Devotee;
import com.temple.donation.model.Donation;
import com.temple.donation.model.Expense;
import com.temple.donation.utils.ExcelReportGenerator;
import com.temple.donation.utils.PdfReportGenerator;

import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {

    private Spinner spReportFY, spReportMonth;
    private Button btnViewReport, btnDownloadPdf, btnDownloadExcel, btnSharePdf, btnShareExcel;
    private Button btnYearlyPdf, btnYearlyExcel;
    private Button btnDevoteesMasterPdf, btnDevoteesMasterExcel;
    private Button btnDonationsMasterPdf, btnDonationsMasterExcel;
    private Button btnExpensesMasterPdf, btnExpensesMasterExcel;
    private View llReportContainer;

    private TextView tvReportTitle, tvReportSubtitle;
    private TextView tvPaidCount, tvPendingCount;
    private TextView tvReportTotalDonation, tvReportTotalExpense, tvReportBalance;
    private RecyclerView rvReportDonations, rvReportExpenses;

    private DonationAdapter donationAdapter;
    private ExpenseAdapter expenseAdapter;

    // Data loaded from Spring Boot; used by the screen and monthly exports.
    private List<Donation> backendDonations = new ArrayList<>();
    private List<Expense> backendExpenses = new ArrayList<>();
    private double backendTotalDonation;
    private double backendTotalExpense;
    private int backendPaidCount;
    private int backendPendingCount;
    private boolean reportLoaded;

    private final String[] financialYears = {"2024-25", "2025-26", "2026-27", "2027-28"};
    private final String[] months = {
            "April", "May", "June", "July", "August", "September",
            "October", "November", "December", "January", "February", "March"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Financial Reports (PDF & Excel)");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();
        setupSpinners();
        setupActionButtons();

        // Auto-load report initially
        generateReportView();
    }

    private void initViews() {
        spReportFY = findViewById(R.id.spReportFY);
        spReportMonth = findViewById(R.id.spReportMonth);
        btnViewReport = findViewById(R.id.btnViewReport);
        btnDownloadPdf = findViewById(R.id.btnDownloadPdf);
        btnDownloadExcel = findViewById(R.id.btnDownloadExcel);
        btnSharePdf = findViewById(R.id.btnSharePdf);
        btnShareExcel = findViewById(R.id.btnShareExcel);

        btnYearlyPdf = findViewById(R.id.btnYearlyPdf);
        btnYearlyExcel = findViewById(R.id.btnYearlyExcel);

        btnDevoteesMasterPdf = findViewById(R.id.btnDevoteesMasterPdf);
        btnDevoteesMasterExcel = findViewById(R.id.btnDevoteesMasterExcel);
        btnDonationsMasterPdf = findViewById(R.id.btnDonationsMasterPdf);
        btnDonationsMasterExcel = findViewById(R.id.btnDonationsMasterExcel);
        btnExpensesMasterPdf = findViewById(R.id.btnExpensesMasterPdf);
        btnExpensesMasterExcel = findViewById(R.id.btnExpensesMasterExcel);

        llReportContainer = findViewById(R.id.llReportContainer);

        tvReportTitle = findViewById(R.id.tvReportTitle);
        tvReportSubtitle = findViewById(R.id.tvReportSubtitle);
        tvPaidCount = findViewById(R.id.tvPaidCount);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvReportTotalDonation = findViewById(R.id.tvReportTotalDonation);
        tvReportTotalExpense = findViewById(R.id.tvReportTotalExpense);
        tvReportBalance = findViewById(R.id.tvReportBalance);

        rvReportDonations = findViewById(R.id.rvReportDonations);
        rvReportExpenses = findViewById(R.id.rvReportExpenses);

        rvReportDonations.setLayoutManager(new LinearLayoutManager(this));
        donationAdapter = new DonationAdapter();
        rvReportDonations.setAdapter(donationAdapter);

        rvReportExpenses.setLayoutManager(new LinearLayoutManager(this));
        expenseAdapter = new ExpenseAdapter(null); // Read only in report
        rvReportExpenses.setAdapter(expenseAdapter);
    }

    private void setupSpinners() {
        ArrayAdapter<String> fyAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, financialYears);
        spReportFY.setAdapter(fyAdapter);
        spReportFY.setSelection(2); // 2026-27

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, months);
        spReportMonth.setAdapter(monthAdapter);
        spReportMonth.setSelection(6); // October
    }

    private void setupActionButtons() {
        btnViewReport.setOnClickListener(v -> generateReportView());

        // Monthly exports
        if (btnDownloadPdf != null) btnDownloadPdf.setOnClickListener(v -> createMonthlyPdf(false));
        if (btnSharePdf != null) btnSharePdf.setOnClickListener(v -> createMonthlyPdf(true));
        if (btnDownloadExcel != null) btnDownloadExcel.setOnClickListener(v -> createMonthlyExcel(false));
        if (btnShareExcel != null) btnShareExcel.setOnClickListener(v -> createMonthlyExcel(true));

        // Yearly exports
        if (btnYearlyPdf != null) btnYearlyPdf.setOnClickListener(v -> createYearlyPdf());
        if (btnYearlyExcel != null) btnYearlyExcel.setOnClickListener(v -> createYearlyExcel());

        // Master tables exports
        if (btnDevoteesMasterPdf != null) btnDevoteesMasterPdf.setOnClickListener(v -> createDevoteesMasterPdf());
        if (btnDevoteesMasterExcel != null) btnDevoteesMasterExcel.setOnClickListener(v -> createDevoteesMasterExcel());
        if (btnDonationsMasterPdf != null) btnDonationsMasterPdf.setOnClickListener(v -> createDonationsMasterPdf());
        if (btnDonationsMasterExcel != null) btnDonationsMasterExcel.setOnClickListener(v -> createDonationsMasterExcel());
        if (btnExpensesMasterPdf != null) btnExpensesMasterPdf.setOnClickListener(v -> createExpensesMasterPdf());
        if (btnExpensesMasterExcel != null) btnExpensesMasterExcel.setOnClickListener(v -> createExpensesMasterExcel());
    }

    private void generateReportView() {
        String fy = spReportFY.getSelectedItem().toString();
        String month = spReportMonth.getSelectedItem().toString();

        tvReportTitle.setText("KANDUAH SATSANG VIHAR");
        tvReportSubtitle.setText(month + " " + fy + " Financial Statement");
        llReportContainer.setVisibility(View.GONE);
        reportLoaded = false;

        String path = "/api/admin/reports/monthly?financialYear=" + fy
                + "&month=" + month;

        BackendApi.get(this, path, new BackendApi.Callback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    backendTotalDonation = json.optDouble("totalDonation", 0.0);
                    backendTotalExpense = json.optDouble("totalExpense", 0.0);
                    backendPaidCount = json.optInt("paidCount", 0);
                    backendPendingCount = json.optInt("pendingCount", 0);
                    backendDonations = parseDonations(json.optJSONArray("donations"));
                    backendExpenses = parseExpenses(json.optJSONArray("expenses"));

                    NumberFormat inr = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
                    tvPaidCount.setText("Devotees Paid: " + backendPaidCount);
                    tvPendingCount.setText("Devotees Pending: " + backendPendingCount);
                    tvReportTotalDonation.setText(inr.format(backendTotalDonation));
                    tvReportTotalExpense.setText(inr.format(backendTotalExpense));
                    tvReportBalance.setText(inr.format(backendTotalDonation - backendTotalExpense));

                    donationAdapter.setDonations(backendDonations);
                    expenseAdapter.setExpenses(backendExpenses);
                    llReportContainer.setVisibility(View.VISIBLE);
                    reportLoaded = true;
                } catch (Exception e) {
                    Toast.makeText(ReportActivity.this,
                            "Unable to read report response: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ReportActivity.this,
                        "Could not load report from server: " + message,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private List<Donation> parseDonations(JSONArray array) throws Exception {
        List<Donation> result = new ArrayList<>();
        if (array == null) return result;

        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.getJSONObject(i);
            Donation donation = new Donation();
            donation.setId(item.optLong("id"));

            JSONObject devotee = item.optJSONObject("devotee");
            donation.setDevoteeId(item.optLong("devoteeId",
                    devotee == null ? 0 : devotee.optLong("id")));
            if (devotee != null) {
                donation.setDevoteeName(devotee.optString("name", ""));
            } else {
                donation.setDevoteeName(item.optString("devoteeName", ""));
            }

            donation.setFinancialYear(item.optString("financialYear", ""));
            donation.setMonth(item.optString("month", ""));
            donation.setAmount(item.optDouble("amount", 0.0));
            donation.setDonationDate(item.optString("donationDate", ""));
            donation.setPaymentMode(item.optString("paymentMode", ""));
            donation.setRemarks(item.optString("remarks", ""));
            result.add(donation);
        }
        return result;
    }

    private List<Expense> parseExpenses(JSONArray array) throws Exception {
        List<Expense> result = new ArrayList<>();
        if (array == null) return result;

        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.getJSONObject(i);
            Expense expense = new Expense();
            expense.setId(item.optLong("id"));
            expense.setFinancialYear(item.optString("financialYear", ""));
            expense.setMonth(item.optString("month", ""));
            expense.setDescription(item.optString("description", ""));
            expense.setAmount(item.optDouble("amount", 0.0));
            expense.setExpenseDate(item.optString("expenseDate", ""));
            expense.setRemarks(item.optString("remarks", ""));
            result.add(expense);
        }
        return result;
    }

    private void createMonthlyPdf(boolean andShare) {
        String fy = spReportFY.getSelectedItem().toString();
        String month = spReportMonth.getSelectedItem().toString();

        if (!reportLoaded) {
            Toast.makeText(this, "Load the monthly report successfully before exporting.", Toast.LENGTH_SHORT).show();
            return;
        }
        List<Donation> donations = new ArrayList<>(backendDonations);
        List<Expense> expenses = new ArrayList<>(backendExpenses);
        double totalDonation = backendTotalDonation;
        double totalExpense = backendTotalExpense;
        int paidCount = backendPaidCount;
        int pendingCount = backendPendingCount;

        File pdfFile = PdfReportGenerator.generateMonthlyPdf(
                this, "KANDUAH SATSANG VIHAR", fy, month,
                donations, totalDonation, expenses, totalExpense, paidCount, pendingCount
        );

        if (pdfFile != null && pdfFile.exists()) {
            if (andShare) {
                shareFile(pdfFile, "application/pdf", "Temple Monthly Report - " + month + " " + fy);
            } else {
                Toast.makeText(this, "PDF saved: " + pdfFile.getName(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Error generating PDF report", Toast.LENGTH_SHORT).show();
        }
    }

    private void createMonthlyExcel(boolean andShare) {
        String fy = spReportFY.getSelectedItem().toString();
        String month = spReportMonth.getSelectedItem().toString();

        if (!reportLoaded) {
            Toast.makeText(this, "Load the monthly report successfully before exporting.", Toast.LENGTH_SHORT).show();
            return;
        }
        List<Donation> donations = new ArrayList<>(backendDonations);
        List<Expense> expenses = new ArrayList<>(backendExpenses);
        double totalDonation = backendTotalDonation;
        double totalExpense = backendTotalExpense;
        int paidCount = backendPaidCount;
        int pendingCount = backendPendingCount;

        File excelFile = ExcelReportGenerator.generateMonthlyExcel(
                this, "KANDUAH SATSANG VIHAR", fy, month,
                donations, totalDonation, expenses, totalExpense, paidCount, pendingCount
        );

        if (excelFile != null && excelFile.exists()) {
            if (andShare) {
                shareFile(excelFile, "text/csv", "Temple Monthly Excel Report - " + month + " " + fy);
            } else {
                Toast.makeText(this, "Excel saved: " + excelFile.getName(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Error generating Excel report", Toast.LENGTH_SHORT).show();
        }
    }


    private void createYearlyPdf() {
        String fy = spReportFY.getSelectedItem().toString();
        loadYearlyData(fy, (devotees, donations, expenses) -> {
            File file = PdfReportGenerator.generateYearlyPdf(
                    this, "KANDUAH SATSANG VIHAR", fy, devotees, donations, expenses);
            finishExport(file, "Annual PDF", "application/pdf",
                    "Temple Annual Report - FY " + fy);
        });
    }

    private void createYearlyExcel() {
        String fy = spReportFY.getSelectedItem().toString();
        loadYearlyData(fy, (devotees, donations, expenses) -> {
            File file = ExcelReportGenerator.generateYearlyExcel(
                    this, "KANDUAH SATSANG VIHAR", fy, devotees, donations, expenses);
            finishExport(file, "Annual Excel", "text/csv",
                    "Temple Annual Excel Report - FY " + fy);
        });
    }

    private interface YearlyDataCallback {
        void onLoaded(List<Devotee> devotees, List<Donation> donations, List<Expense> expenses);
    }

    private void loadYearlyData(String fy, YearlyDataCallback callback) {
        setExportButtonsEnabled(false);
        BackendApi.get(this, "/api/admin/reports/master/devotees", new BackendApi.Callback() {
            @Override public void onSuccess(String devoteesJson) {
                try {
                    List<Devotee> devotees = parseDevotees(extractArray(devoteesJson,
                            "devotees", "data", "content"));
                    BackendApi.get(ReportActivity.this,
                            "/api/admin/donations?financialYear=" + encode(fy),
                            new BackendApi.Callback() {
                                @Override public void onSuccess(String donationsJson) {
                                    try {
                                        List<Donation> donations = parseDonations(extractArray(donationsJson,
                                                "donations", "data", "content"));
                                        BackendApi.get(ReportActivity.this,
                                                "/api/admin/expenses?financialYear=" + encode(fy),
                                                new BackendApi.Callback() {
                                                    @Override public void onSuccess(String expensesJson) {
                                                        try {
                                                            List<Expense> expenses = parseExpenses(extractArray(
                                                                    expensesJson, "expenses", "data", "content"));
                                                            setExportButtonsEnabled(true);
                                                            callback.onLoaded(devotees, donations, expenses);
                                                        } catch (Exception e) { exportError(e); }
                                                    }
                                                    @Override public void onError(String message) { exportError(message); }
                                                });
                                    } catch (Exception e) { exportError(e); }
                                }
                                @Override public void onError(String message) { exportError(message); }
                            });
                } catch (Exception e) { exportError(e); }
            }
            @Override public void onError(String message) { exportError(message); }
        });
    }

    private void createDevoteesMasterPdf() {
        loadMasterData((devotees, donations, expenses) -> {
            File file = PdfReportGenerator.generateDevoteesMasterPdf(
                    this, "KANDUAH SATSANG VIHAR", devotees, donations);
            finishExport(file, "Master Devotees PDF", "application/pdf",
                    "Temple Master Devotee Directory");
        });
    }

    private void createDevoteesMasterExcel() {
        loadMasterData((devotees, donations, expenses) -> {
            File file = ExcelReportGenerator.generateDevoteesMasterExcel(
                    this, "KANDUAH SATSANG VIHAR", devotees, donations);
            finishExport(file, "Master Devotees Excel", "text/csv",
                    "Temple Master Devotee Directory Excel");
        });
    }

    private void createDonationsMasterPdf() {
        loadMasterData((devotees, donations, expenses) -> {
            File file = PdfReportGenerator.generateDonationsMasterPdf(
                    this, "KANDUAH SATSANG VIHAR", donations, devotees);
            finishExport(file, "Master Donations PDF", "application/pdf",
                    "Temple Master Donations Ledger");
        });
    }

    private void createDonationsMasterExcel() {
        loadMasterData((devotees, donations, expenses) -> {
            File file = ExcelReportGenerator.generateDonationsMasterExcel(
                    this, "KANDUAH SATSANG VIHAR", donations, devotees);
            finishExport(file, "Master Donations Excel", "text/csv",
                    "Temple Master Donations Ledger Excel");
        });
    }

    private void createExpensesMasterPdf() {
        loadMasterData((devotees, donations, expenses) -> {
            File file = PdfReportGenerator.generateExpensesMasterPdf(
                    this, "KANDUAH SATSANG VIHAR", expenses);
            finishExport(file, "Master Expenses PDF", "application/pdf",
                    "Temple Master Expenses Ledger");
        });
    }

    private void createExpensesMasterExcel() {
        loadMasterData((devotees, donations, expenses) -> {
            File file = ExcelReportGenerator.generateExpensesMasterExcel(
                    this, "KANDUAH SATSANG VIHAR", expenses);
            finishExport(file, "Master Expenses Excel", "text/csv",
                    "Temple Master Expenses Ledger Excel");
        });
    }

    private void loadMasterData(YearlyDataCallback callback) {
        setExportButtonsEnabled(false);
        BackendApi.get(this, "/api/admin/reports/master/devotees", new BackendApi.Callback() {
            @Override public void onSuccess(String devoteesJson) {
                try {
                    List<Devotee> devotees = parseDevotees(extractArray(devoteesJson,
                            "devotees", "data", "content"));
                    BackendApi.get(ReportActivity.this, "/api/admin/reports/master/donations",
                            new BackendApi.Callback() {
                                @Override public void onSuccess(String donationsJson) {
                                    try {
                                        List<Donation> donations = parseDonations(extractArray(
                                                donationsJson, "donations", "data", "content"));
                                        BackendApi.get(ReportActivity.this,
                                                "/api/admin/reports/master/expenses",
                                                new BackendApi.Callback() {
                                                    @Override public void onSuccess(String expensesJson) {
                                                        try {
                                                            List<Expense> expenses = parseExpenses(extractArray(
                                                                    expensesJson, "expenses", "data", "content"));
                                                            setExportButtonsEnabled(true);
                                                            callback.onLoaded(devotees, donations, expenses);
                                                        } catch (Exception e) { exportError(e); }
                                                    }
                                                    @Override public void onError(String message) { exportError(message); }
                                                });
                                    } catch (Exception e) { exportError(e); }
                                }
                                @Override public void onError(String message) { exportError(message); }
                            });
                } catch (Exception e) { exportError(e); }
            }
            @Override public void onError(String message) { exportError(message); }
        });
    }

    private JSONArray extractArray(String response, String... keys) throws Exception {
        Object parsed = new org.json.JSONTokener(response).nextValue();
        if (parsed instanceof JSONArray) return (JSONArray) parsed;
        if (parsed instanceof JSONObject) {
            JSONObject object = (JSONObject) parsed;
            for (String key : keys) {
                Object value = object.opt(key);
                if (value instanceof JSONArray) return (JSONArray) value;
                if (value instanceof JSONObject) {
                    JSONObject nested = (JSONObject) value;
                    for (String nestedKey : keys) {
                        Object nestedValue = nested.opt(nestedKey);
                        if (nestedValue instanceof JSONArray) return (JSONArray) nestedValue;
                    }
                }
            }
        }
        return new JSONArray();
    }

    private List<Devotee> parseDevotees(JSONArray array) throws Exception {
        List<Devotee> result = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.getJSONObject(i);
            Devotee d = new Devotee();
            d.setId((int) item.optLong("id"));
            d.setName(item.optString("name", ""));
            d.setMobile(item.optString("mobile", ""));
            d.setAddress(item.optString("address", ""));
            d.setMonthlyAmount(item.optDouble("monthlyAmount", 0.0));
            result.add(d);
        }
        return result;
    }

    private String encode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception ignored) {
            return value;
        }
    }

    private void finishExport(File file, String label, String mimeType, String subject) {
        if (file != null && file.exists()) {
            Toast.makeText(this, label + " saved: " + file.getName(), Toast.LENGTH_LONG).show();
            shareFile(file, mimeType, subject);
        } else {
            Toast.makeText(this, "Could not generate " + label, Toast.LENGTH_SHORT).show();
        }
    }

    private void exportError(Exception e) {
        exportError(e == null ? "Unknown error" : e.getMessage());
    }

    private void exportError(String message) {
        runOnUiThread(() -> {
            setExportButtonsEnabled(true);
            Toast.makeText(this, "Could not load export data: " + message,
                    Toast.LENGTH_LONG).show();
        });
    }

    private void setExportButtonsEnabled(boolean enabled) {
        runOnUiThread(() -> {
            if (btnYearlyPdf != null) btnYearlyPdf.setEnabled(enabled);
            if (btnYearlyExcel != null) btnYearlyExcel.setEnabled(enabled);
            if (btnDevoteesMasterPdf != null) btnDevoteesMasterPdf.setEnabled(enabled);
            if (btnDevoteesMasterExcel != null) btnDevoteesMasterExcel.setEnabled(enabled);
            if (btnDonationsMasterPdf != null) btnDonationsMasterPdf.setEnabled(enabled);
            if (btnDonationsMasterExcel != null) btnDonationsMasterExcel.setEnabled(enabled);
            if (btnExpensesMasterPdf != null) btnExpensesMasterPdf.setEnabled(enabled);
            if (btnExpensesMasterExcel != null) btnExpensesMasterExcel.setEnabled(enabled);
        });
    }

    private void shareFile(File file, String mimeType, String subject) {
        try {
            Uri uri = FileProvider.getUriForFile(
                    this,
                    "com.temple.donation.fileprovider",
                    file
            );

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType(mimeType);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Please find attached the report from Kanduah Satsang Vihar.");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Share Report"));
        } catch (Exception e) {
            Toast.makeText(this, "Could not share file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

