package com.temple.donation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.temple.donation.R;
import com.temple.donation.network.BackendApi;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Spinner spFinancialYear, spMonth;
    private TextView tvTotalDonation, tvTotalExpense, tvBalance, tvSelectedMonthHeader;
    private Button btnManageDevotees, btnMonthlyDonation, btnExpenses, btnMonthlyReport, btnFreshStart;

    private final String[] financialYears = {"2024-25", "2025-26", "2026-27", "2027-28"};
    private final String[] months = {
            "April", "May", "June", "July", "August", "September",
            "October", "November", "December", "January", "February", "March"
    };

    private boolean dashboardViewsReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This activity is the ADMIN dashboard. Do not allow a DEVOTEE token here.
        String role = BackendApi.getRole(this);
        if (!BackendApi.isLoggedIn(this) || !"ADMIN".equalsIgnoreCase(role)) {
            BackendApi.logout(this);
            Toast.makeText(this, "Please sign in with an Admin account.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v ->
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Confirm Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Logout", (dialog, which) -> {

                            // Clear the actual authentication session and JWT
                            BackendApi.logout(MainActivity.this);

                            Toast.makeText(
                                    MainActivity.this,
                                    "Logged out successfully",
                                    Toast.LENGTH_SHORT
                            ).show();

                            // Return to login selection and clear dashboard history
                            Intent intent = new Intent(
                                    MainActivity.this,
                                    LoginSelectionActivity.class
                            );

                            intent.setFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK
                                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
                            );

                            startActivity(intent);
                            finish();
                        })
                        .show()
        );
        initViews();
        setupSpinners();
        setupClickListeners();
        dashboardViewsReady = true;
        refreshDashboardData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dashboardViewsReady) refreshDashboardData();
    }

    private void initViews() {
        spFinancialYear = findViewById(R.id.spFinancialYear);
        spMonth = findViewById(R.id.spMonth);
        tvTotalDonation = findViewById(R.id.tvTotalDonation);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvBalance = findViewById(R.id.tvBalance);
        tvSelectedMonthHeader = findViewById(R.id.tvSelectedMonthHeader);

        btnManageDevotees = findViewById(R.id.btnManageDevotees);
        btnMonthlyDonation = findViewById(R.id.btnMonthlyDonation);
        btnExpenses = findViewById(R.id.btnExpenses);
        btnMonthlyReport = findViewById(R.id.btnMonthlyReport);
        btnFreshStart = findViewById(R.id.btnFreshStart);
    }

    private void setupSpinners() {
        ArrayAdapter<String> fyAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, financialYears);
        fyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFinancialYear.setAdapter(fyAdapter);
        spFinancialYear.setSelection(2); // 2026-27

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMonth.setAdapter(monthAdapter);
        spMonth.setSelection(6); // October

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshDashboardData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        };

        spFinancialYear.setOnItemSelectedListener(listener);
        spMonth.setOnItemSelectedListener(listener);
    }

    private void setupClickListeners() {
        btnManageDevotees.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, DevoteeActivity.class)));

        btnMonthlyDonation.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DonationActivity.class);
            intent.putExtra("selected_fy", selectedFinancialYear());
            intent.putExtra("selected_month", selectedMonth());
            startActivity(intent);
        });

        btnExpenses.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ExpenseActivity.class);
            intent.putExtra("selected_fy", selectedFinancialYear());
            intent.putExtra("selected_month", selectedMonth());
            startActivity(intent);
        });

        btnMonthlyReport.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ReportActivity.class);
            intent.putExtra("selected_fy", selectedFinancialYear());
            intent.putExtra("selected_month", selectedMonth());
            startActivity(intent);
        });

        if (btnFreshStart != null) {
            btnFreshStart.setOnClickListener(v -> showFreshStartOptions());
        }
    }

    private String selectedFinancialYear() {
        return spFinancialYear.getSelectedItem() == null
                ? financialYears[2] : spFinancialYear.getSelectedItem().toString();
    }

    private String selectedMonth() {
        return spMonth.getSelectedItem() == null
                ? months[6] : spMonth.getSelectedItem().toString();
    }

    private void showFreshStartOptions() {
        String[] options = {
                "Fresh Start (Clear Entire Database)",
                "Reset to Sample Template Data"
        };

        new AlertDialog.Builder(this)
                .setTitle("Admin Database Management")
                .setMessage("These operations require dedicated secured backend endpoints. They are disabled to avoid clearing only local data while the app uses PostgreSQL.")
                .setItems(options, (dialog, which) ->
                        Toast.makeText(this,
                                "This action is not connected. Add a secured backend admin endpoint first.",
                                Toast.LENGTH_LONG).show())
                .setNegativeButton("Close", null)
                .show();
    }

    private void refreshDashboardData() {
        if (!dashboardViewsReady || spFinancialYear == null || spMonth == null
                || tvSelectedMonthHeader == null) {
            return;
        }

        String fy = selectedFinancialYear();
        String month = selectedMonth();
        tvSelectedMonthHeader.setText(month + " " + fy);

        NumberFormat inrFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        String path = "/api/admin/dashboard?financialYear=" + fy + "&month=" + month;

        BackendApi.get(this, path, new BackendApi.Callback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);

                    double totalDonation = json.optDouble("totalDonation",
                            json.optDouble("totalDonations", 0.0));
                    double totalExpense = json.optDouble("totalExpense",
                            json.optDouble("totalExpenses", 0.0));
                    double balance = json.has("balance")
                            ? json.optDouble("balance", totalDonation - totalExpense)
                            : totalDonation - totalExpense;

                    tvTotalDonation.setText(inrFormat.format(totalDonation));
                    tvTotalExpense.setText(inrFormat.format(totalExpense));
                    tvBalance.setText(inrFormat.format(balance));
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this,
                            "Invalid dashboard response from backend",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(MainActivity.this,
                        "Could not load dashboard: " + message,
                        Toast.LENGTH_LONG).show();

                if (message != null && message.contains("HTTP 401")) {
                    BackendApi.logout(MainActivity.this);
                    finish();
                }
            }
        });
    }
}
