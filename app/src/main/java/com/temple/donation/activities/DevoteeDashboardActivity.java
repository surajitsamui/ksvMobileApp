package com.temple.donation.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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
import com.temple.donation.model.Devotee;
import com.temple.donation.model.Donation;
import com.temple.donation.model.Expense;
import com.temple.donation.network.BackendApi;
import com.temple.donation.utils.ExcelReportGenerator;
import com.temple.donation.utils.PdfReportGenerator;
import com.temple.donation.utils.ReportPublishManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DevoteeDashboardActivity extends AppCompatActivity {

    private static final String[] FINANCIAL_YEARS = {
            "2024-25", "2025-26", "2026-27", "2027-28"
    };

    private static final String[] MONTHS = {
            "April", "May", "June", "July", "August", "September",
            "October", "November", "December", "January", "February", "March"
    };

    private Spinner spDevoteeFY, spDevoteeMonth;
    private Button btnDevoteeRefresh, btnDevoteeLogout, btnHeaderLogout;

    private TextView tvPersonalSummary, tvMyDonationReportText;
    private TextView tvDevoteePublishStatus, tvPendingNoticeDetail;
    private TextView tvDevoteeTotalDonation, tvDevoteeTotalExpense, tvDevoteeBalance;
    private View llDevoteeReportPendingNotice, llDevoteeMonthlyReportContainer;

    private TextView tvDevoteeYearlyStatus;
    private View llDevoteeYearlyPendingNotice, llDevoteeYearlyReportContainer;

    private Button btnDevoteeDownloadPdf, btnDevoteeDownloadExcel, btnDevoteeSharePdf, btnDevoteeShareExcel;
    private Button btnDevoteeYearlyPdf, btnDevoteeYearlyExcel;

    private RecyclerView rvDevoteeTempleDonations, rvDevoteeTempleExpenses;
    private DonationAdapter templeDonationAdapter;
    private ExpenseAdapter templeExpenseAdapter;

    private String currentFinancialYear = "2026-27";
    private String currentMonth = "October";

    // Loaded report data for export
    private List<Donation> backendDonations = new ArrayList<>();
    private List<Expense> backendExpenses = new ArrayList<>();
    private double backendTotalDonation = 0;
    private double backendTotalExpense = 0;
    private int backendPaidCount = 0;
    private int backendPendingCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!BackendApi.isLoggedIn(this)
                || !"DEVOTEE".equalsIgnoreCase(BackendApi.getRole(this))) {
            BackendApi.logout(this);
            Toast.makeText(this, "Please sign in with a Devotee account.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_devotee_dashboard);

        initViews();
        setupSpinners();
        setupActionButtons();

        loadDashboardData();
    }

    private void initViews() {
        spDevoteeFY = findViewById(R.id.spDevoteeFY);
        spDevoteeMonth = findViewById(R.id.spDevoteeMonth);
        btnDevoteeRefresh = findViewById(R.id.btnDevoteeRefresh);
        btnDevoteeLogout = findViewById(R.id.btnDevoteeLogout);
        btnHeaderLogout = findViewById(R.id.btnHeaderLogout);

        tvPersonalSummary = findViewById(R.id.tvPersonalSummary);
        tvMyDonationReportText = findViewById(R.id.tvMyDonationReportText);

        tvDevoteePublishStatus = findViewById(R.id.tvDevoteePublishStatus);
        tvPendingNoticeDetail = findViewById(R.id.tvPendingNoticeDetail);
        llDevoteeReportPendingNotice = findViewById(R.id.llDevoteeReportPendingNotice);
        llDevoteeMonthlyReportContainer = findViewById(R.id.llDevoteeMonthlyReportContainer);

        tvDevoteeTotalDonation = findViewById(R.id.tvDevoteeTotalDonation);
        tvDevoteeTotalExpense = findViewById(R.id.tvDevoteeTotalExpense);
        tvDevoteeBalance = findViewById(R.id.tvDevoteeBalance);

        rvDevoteeTempleDonations = findViewById(R.id.rvDevoteeTempleDonations);
        rvDevoteeTempleExpenses = findViewById(R.id.rvDevoteeTempleExpenses);

        if (rvDevoteeTempleDonations != null) {
            rvDevoteeTempleDonations.setLayoutManager(new LinearLayoutManager(this));
            templeDonationAdapter = new DonationAdapter();
            rvDevoteeTempleDonations.setAdapter(templeDonationAdapter);
        }

        if (rvDevoteeTempleExpenses != null) {
            rvDevoteeTempleExpenses.setLayoutManager(new LinearLayoutManager(this));
            templeExpenseAdapter = new ExpenseAdapter(null); // Read only in report
            rvDevoteeTempleExpenses.setAdapter(templeExpenseAdapter);
        }

        tvDevoteeYearlyStatus = findViewById(R.id.tvDevoteeYearlyStatus);
        llDevoteeYearlyPendingNotice = findViewById(R.id.llDevoteeYearlyPendingNotice);
        llDevoteeYearlyReportContainer = findViewById(R.id.llDevoteeYearlyReportContainer);

        btnDevoteeDownloadPdf = findViewById(R.id.btnDevoteeDownloadPdf);
        btnDevoteeDownloadExcel = findViewById(R.id.btnDevoteeDownloadExcel);
        btnDevoteeSharePdf = findViewById(R.id.btnDevoteeSharePdf);
        btnDevoteeShareExcel = findViewById(R.id.btnDevoteeShareExcel);

        btnDevoteeYearlyPdf = findViewById(R.id.btnDevoteeYearlyPdf);
        btnDevoteeYearlyExcel = findViewById(R.id.btnDevoteeYearlyExcel);
    }

    private void setupSpinners() {
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, FINANCIAL_YEARS);
        spDevoteeFY.setAdapter(yearAdapter);
        spDevoteeFY.setSelection(2); // 2026-27

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, MONTHS);
        spDevoteeMonth.setAdapter(monthAdapter);
        spDevoteeMonth.setSelection(6); // October

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadDashboardData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        };

        spDevoteeFY.setOnItemSelectedListener(listener);
        spDevoteeMonth.setOnItemSelectedListener(listener);
    }

    private void setupActionButtons() {
        btnDevoteeRefresh.setOnClickListener(v -> loadDashboardData());

        View.OnClickListener logoutListener = v -> {
            BackendApi.logout(this);
            Intent intent = new Intent(this, LoginSelectionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        };

        if (btnDevoteeLogout != null) btnDevoteeLogout.setOnClickListener(logoutListener);
        if (btnHeaderLogout != null) btnHeaderLogout.setOnClickListener(logoutListener);

        // Monthly exports
        if (btnDevoteeDownloadPdf != null) btnDevoteeDownloadPdf.setOnClickListener(v -> createMonthlyPdf(false));
        if (btnDevoteeSharePdf != null) btnDevoteeSharePdf.setOnClickListener(v -> createMonthlyPdf(true));
        if (btnDevoteeDownloadExcel != null) btnDevoteeDownloadExcel.setOnClickListener(v -> createMonthlyExcel(false));
        if (btnDevoteeShareExcel != null) btnDevoteeShareExcel.setOnClickListener(v -> createMonthlyExcel(true));

        // Yearly exports
        if (btnDevoteeYearlyPdf != null) btnDevoteeYearlyPdf.setOnClickListener(v -> createYearlyPdf());
        if (btnDevoteeYearlyExcel != null) btnDevoteeYearlyExcel.setOnClickListener(v -> createYearlyExcel());
    }

    private void loadDashboardData() {
        currentFinancialYear = spDevoteeFY.getSelectedItem().toString();
        currentMonth = spDevoteeMonth.getSelectedItem().toString();

        loadPersonalDonations();
        checkAndLoadTempleMonthlyReport();
        checkAndLoadTempleYearlyReport();
    }

    // =========================================================================
    // SECTION 1: DEVOTEE'S OWN PERSONAL DONATION DETAILS
    // =========================================================================
    private void loadPersonalDonations() {
        tvPersonalSummary.setText("Fetching your personal donation history for FY " + currentFinancialYear + "...");
        tvMyDonationReportText.setText("Loading...");

        String path = "/api/devotee/donations?financialYear=" + currentFinancialYear;

        BackendApi.get(this, path, new BackendApi.Callback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONArray records = new JSONArray(response);
                    StringBuilder report = new StringBuilder();
                    double totalAmount = 0;
                    int countPaid = 0;

                    if (records.length() == 0) {
                        report.append("No personal donation records found for Financial Year ")
                                .append(currentFinancialYear).append(".");
                    }

                    for (int i = 0; i < records.length(); i++) {
                        JSONObject item = records.getJSONObject(i);
                        String m = item.optString("month", "N/A");
                        double amount = item.optDouble("amount", 0);
                        String date = item.optString("donationDate", "N/A");
                        String paymentMode = item.optString("paymentMode", "N/A");
                        String remarks = item.optString("remarks", "");

                        report.append("• ").append(m).append(" (FY ").append(currentFinancialYear).append(")")
                                .append("\n   Amount: ").append(formatMoney(amount))
                                .append(" | Mode: ").append(paymentMode)
                                .append("\n   Date: ").append(date);
                        if (!remarks.isEmpty()) {
                            report.append(" | Remarks: ").append(remarks);
                        }
                        report.append("\n\n");

                        totalAmount += amount;
                        countPaid++;
                    }

                    tvPersonalSummary.setText("FY " + currentFinancialYear + " Personal Total: "
                            + formatMoney(totalAmount) + " (" + countPaid + " months recorded)");
                    tvMyDonationReportText.setText(report.toString().trim());

                } catch (Exception e) {
                    tvPersonalSummary.setText("Personal donation history loaded.");
                    tvMyDonationReportText.setText("Unable to parse personal donation response: " + e.getMessage());
                }
            }

            @Override
            public void onError(String message) {
                tvPersonalSummary.setText("Personal donation status");
                tvMyDonationReportText.setText("Notice: " + message);
            }
        });
    }

    // =========================================================================
    // SECTION 2: TEMPLE MONTHLY FINANCIAL REPORT (PUBLISHED VIEW)
    // =========================================================================
    private void checkAndLoadTempleMonthlyReport() {
        ReportPublishManager.fetchStatusFromBackend(this, currentFinancialYear, currentMonth, isPublished -> {
            if (isPublished) {
                tvDevoteePublishStatus.setText("PUBLISHED");
                tvDevoteePublishStatus.setTextColor(0xFF16A34A);
                tvDevoteePublishStatus.setBackgroundResource(R.drawable.bg_status_paid);

                llDevoteeReportPendingNotice.setVisibility(View.GONE);
                llDevoteeMonthlyReportContainer.setVisibility(View.VISIBLE);

                loadPublishedMonthlyReportData();
            } else {
                tvDevoteePublishStatus.setText("NOT PUBLISHED");
                tvDevoteePublishStatus.setTextColor(0xFFEA580C);
                tvDevoteePublishStatus.setBackgroundResource(R.drawable.bg_status_pending);

                if (tvPendingNoticeDetail != null) {
                    tvPendingNoticeDetail.setText("The temple administration has not published the financial statement for "
                            + currentMonth + " " + currentFinancialYear + " yet. Once published by admin, the complete statement and PDF/Excel download options will appear here.");
                }

                llDevoteeReportPendingNotice.setVisibility(View.VISIBLE);
                llDevoteeMonthlyReportContainer.setVisibility(View.GONE);
            }
        });
    }

    private void loadPublishedMonthlyReportData() {
        tvDevoteeTotalDonation.setText("Loading...");
        tvDevoteeTotalExpense.setText("Loading...");
        tvDevoteeBalance.setText("Loading...");

        String path = "/api/devotee/dashboard?financialYear=" + currentFinancialYear
                + "&month=" + currentMonth;

        BackendApi.get(this, path, new BackendApi.Callback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject data = new JSONObject(response);

                    backendTotalDonation = data.optDouble("totalDonation", data.optDouble("totalDonations", 0));
                    backendTotalExpense = data.optDouble("totalExpense", data.optDouble("totalExpenses", 0));
                    double balance = data.optDouble("balance", backendTotalDonation - backendTotalExpense);
                    backendPaidCount = data.optInt("paidCount", 0);
                    backendPendingCount = data.optInt("pendingCount", 0);

                    backendDonations = parseDonations(data.optJSONArray("donations"));
                    backendExpenses = parseExpenses(data.optJSONArray("expenses"));

                    tvDevoteeTotalDonation.setText(formatMoney(backendTotalDonation));
                    tvDevoteeTotalExpense.setText(formatMoney(backendTotalExpense));
                    tvDevoteeBalance.setText(formatMoney(balance));

                    if (templeDonationAdapter != null) {
                        templeDonationAdapter.setDonations(backendDonations);
                    }
                    if (templeExpenseAdapter != null) {
                        templeExpenseAdapter.setExpenses(backendExpenses);
                    }
                } catch (Exception e) {
                    tvDevoteeTotalDonation.setText("--");
                    tvDevoteeTotalExpense.setText("--");
                    tvDevoteeBalance.setText("--");
                }
            }

            @Override
            public void onError(String message) {
                // Fallback to general admin report endpoint if allowed by backend
                fetchMonthlyReportFallback();
            }
        });
    }

    private void fetchMonthlyReportFallback() {
        String path = "/api/admin/reports/monthly?financialYear=" + currentFinancialYear + "&month=" + currentMonth;
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

                    tvDevoteeTotalDonation.setText(formatMoney(backendTotalDonation));
                    tvDevoteeTotalExpense.setText(formatMoney(backendTotalExpense));
                    tvDevoteeBalance.setText(formatMoney(backendTotalDonation - backendTotalExpense));

                    if (templeDonationAdapter != null) {
                        templeDonationAdapter.setDonations(backendDonations);
                    }
                    if (templeExpenseAdapter != null) {
                        templeExpenseAdapter.setExpenses(backendExpenses);
                    }
                } catch (Exception ignored) {}
            }

            @Override public void onError(String message) {}
        });
    }

    // =========================================================================
    // SECTION 3: TEMPLE ANNUAL (YEARLY) FINANCIAL REPORT
    // =========================================================================
    private void checkAndLoadTempleYearlyReport() {
        boolean isYearlyPublished = ReportPublishManager.isYearlyReportPublished(this, currentFinancialYear);

        if (isYearlyPublished) {
            tvDevoteeYearlyStatus.setText("PUBLISHED");
            tvDevoteeYearlyStatus.setTextColor(0xFF16A34A);
            tvDevoteeYearlyStatus.setBackgroundResource(R.drawable.bg_status_paid);

            llDevoteeYearlyPendingNotice.setVisibility(View.GONE);
            llDevoteeYearlyReportContainer.setVisibility(View.VISIBLE);
        } else {
            tvDevoteeYearlyStatus.setText("NOT PUBLISHED");
            tvDevoteeYearlyStatus.setTextColor(0xFFEA580C);
            tvDevoteeYearlyStatus.setBackgroundResource(R.drawable.bg_status_pending);

            llDevoteeYearlyPendingNotice.setVisibility(View.VISIBLE);
            llDevoteeYearlyReportContainer.setVisibility(View.GONE);
        }
    }

    // =========================================================================
    // EXPORT & REPORT GENERATION HANDLERS
    // =========================================================================
    private void createMonthlyPdf(boolean andShare) {
        if (!ReportPublishManager.isReportPublished(this, currentFinancialYear, currentMonth)) {
            Toast.makeText(this, "The monthly report for " + currentMonth + " " + currentFinancialYear + " has not been published by admin yet.", Toast.LENGTH_LONG).show();
            return;
        }

        File pdfFile = PdfReportGenerator.generateMonthlyPdf(
                this, "KANDUAH SATSANG VIHAR", currentFinancialYear, currentMonth,
                backendDonations, backendTotalDonation, backendExpenses, backendTotalExpense,
                backendPaidCount, backendPendingCount
        );

        if (pdfFile != null && pdfFile.exists()) {
            if (andShare) {
                shareFile(pdfFile, "application/pdf", "Temple Monthly Report - " + currentMonth + " " + currentFinancialYear);
            } else {
                Toast.makeText(this, "PDF saved: " + pdfFile.getName(), Toast.LENGTH_LONG).show();
                viewFile(pdfFile, "application/pdf");
            }
        } else {
            Toast.makeText(this, "Could not generate monthly PDF report", Toast.LENGTH_SHORT).show();
        }
    }

    private void createMonthlyExcel(boolean andShare) {
        if (!ReportPublishManager.isReportPublished(this, currentFinancialYear, currentMonth)) {
            Toast.makeText(this, "The monthly report for " + currentMonth + " " + currentFinancialYear + " has not been published by admin yet.", Toast.LENGTH_LONG).show();
            return;
        }

        File excelFile = ExcelReportGenerator.generateMonthlyExcel(
                this, "KANDUAH SATSANG VIHAR", currentFinancialYear, currentMonth,
                backendDonations, backendTotalDonation, backendExpenses, backendTotalExpense,
                backendPaidCount, backendPendingCount
        );

        if (excelFile != null && excelFile.exists()) {
            if (andShare) {
                shareFile(excelFile, "text/csv", "Temple Monthly Excel Report - " + currentMonth + " " + currentFinancialYear);
            } else {
                Toast.makeText(this, "Excel saved: " + excelFile.getName(), Toast.LENGTH_LONG).show();
                viewFile(excelFile, "text/csv");
            }
        } else {
            Toast.makeText(this, "Could not generate monthly Excel report", Toast.LENGTH_SHORT).show();
        }
    }

    private void createYearlyPdf() {
        if (!ReportPublishManager.isYearlyReportPublished(this, currentFinancialYear)) {
            Toast.makeText(this, "Annual report for " + currentFinancialYear + " is not published yet.", Toast.LENGTH_LONG).show();
            return;
        }

        loadYearlyData(currentFinancialYear, (devotees, donations, expenses) -> {
            File file = PdfReportGenerator.generateYearlyPdf(
                    this, "KANDUAH SATSANG VIHAR", currentFinancialYear, devotees, donations, expenses);
            if (file != null && file.exists()) {
                Toast.makeText(this, "Yearly PDF saved: " + file.getName(), Toast.LENGTH_LONG).show();
                shareFile(file, "application/pdf", "Temple Annual Report - FY " + currentFinancialYear);
            }
        });
    }

    private void createYearlyExcel() {
        if (!ReportPublishManager.isYearlyReportPublished(this, currentFinancialYear)) {
            Toast.makeText(this, "Annual report for " + currentFinancialYear + " is not published yet.", Toast.LENGTH_LONG).show();
            return;
        }

        loadYearlyData(currentFinancialYear, (devotees, donations, expenses) -> {
            File file = ExcelReportGenerator.generateYearlyExcel(
                    this, "KANDUAH SATSANG VIHAR", currentFinancialYear, devotees, donations, expenses);
            if (file != null && file.exists()) {
                Toast.makeText(this, "Yearly Excel saved: " + file.getName(), Toast.LENGTH_LONG).show();
                shareFile(file, "text/csv", "Temple Annual Excel Report - FY " + currentFinancialYear);
            }
        });
    }

    private interface YearlyDataCallback {
        void onLoaded(List<Devotee> devotees, List<Donation> donations, List<Expense> expenses);
    }

    private void loadYearlyData(String fy, YearlyDataCallback callback) {
        BackendApi.get(this, "/api/admin/reports/master/devotees", new BackendApi.Callback() {
            @Override public void onSuccess(String devoteesJson) {
                try {
                    List<Devotee> devotees = parseDevotees(extractArray(devoteesJson, "devotees", "data", "content"));
                    BackendApi.get(DevoteeDashboardActivity.this, "/api/admin/donations?financialYear=" + encode(fy),
                            new BackendApi.Callback() {
                                @Override public void onSuccess(String donationsJson) {
                                    try {
                                        List<Donation> donations = parseDonations(extractArray(donationsJson, "donations", "data", "content"));
                                        BackendApi.get(DevoteeDashboardActivity.this, "/api/admin/expenses?financialYear=" + encode(fy),
                                                new BackendApi.Callback() {
                                                    @Override public void onSuccess(String expensesJson) {
                                                        try {
                                                            List<Expense> expenses = parseExpenses(extractArray(expensesJson, "expenses", "data", "content"));
                                                            callback.onLoaded(devotees, donations, expenses);
                                                        } catch (Exception e) {
                                                            callback.onLoaded(new ArrayList<>(), donations, new ArrayList<>());
                                                        }
                                                    }
                                                    @Override public void onError(String message) {
                                                        callback.onLoaded(devotees, donations, new ArrayList<>());
                                                    }
                                                });
                                    } catch (Exception e) {
                                        callback.onLoaded(devotees, new ArrayList<>(), new ArrayList<>());
                                    }
                                }
                                @Override public void onError(String message) {
                                    callback.onLoaded(devotees, new ArrayList<>(), new ArrayList<>());
                                }
                            });
                } catch (Exception e) {
                    callback.onLoaded(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
                }
            }
            @Override public void onError(String message) {
                callback.onLoaded(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
            }
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
            }
        }
        return new JSONArray();
    }

    private List<Devotee> parseDevotees(JSONArray array) throws Exception {
        List<Devotee> result = new ArrayList<>();
        if (array == null) return result;
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

    private List<Donation> parseDonations(JSONArray array) throws Exception {
        List<Donation> result = new ArrayList<>();
        if (array == null) return result;
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.getJSONObject(i);
            Donation donation = new Donation();
            donation.setId(item.optLong("id"));

            JSONObject devotee = item.optJSONObject("devotee");
            donation.setDevoteeId(item.optLong("devoteeId", devotee == null ? 0 : devotee.optLong("id")));
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

    private void shareFile(File file, String mimeType, String subject) {
        try {
            Uri uri = FileProvider.getUriForFile(
                    this, "com.temple.donation.fileprovider", file);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType(mimeType);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Please find attached the published temple report from Kanduah Satsang Vihar.");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Share Temple Report"));
        } catch (Exception e) {
            Toast.makeText(this, "Could not share file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void viewFile(File file, String mimeType) {
        try {
            Uri uri = FileProvider.getUriForFile(
                    this, "com.temple.donation.fileprovider", file);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception ignored) {}
    }

    private String encode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception ignored) {
            return value;
        }
    }

    private String formatMoney(double amount) {
        return String.format(Locale.getDefault(), "₹%,.2f", amount);
    }
}
