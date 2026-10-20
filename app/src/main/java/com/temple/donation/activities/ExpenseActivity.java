package com.temple.donation.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.temple.donation.R;
import com.temple.donation.adapters.ExpenseAdapter;
import com.temple.donation.model.Expense;
import com.temple.donation.network.BackendApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Expense management screen backed by the Spring Boot REST API.
 *
 * Expected endpoints:
 * GET    /api/admin/expenses?financialYear=2026-27&month=October
 * POST   /api/admin/expenses
 * DELETE /api/admin/expenses/{id}
 *
 * Expected expense JSON fields: id, financialYear, month, description, amount,
 * expenseDate, remarks. expenseDate is ISO format yyyy-MM-dd in API JSON.
 */
public class ExpenseActivity extends AppCompatActivity {
    private static final String EXPENSES_PATH = "/api/admin/expenses";

    private Spinner spExpenseFY, spExpenseMonth;
    private TextView tvTotalExpenseAmount;
    private RecyclerView rvExpenses;
    private FloatingActionButton fabAddExpense;

    private ExpenseAdapter adapter;
    private List<Expense> currentExpenses = new ArrayList<>();

    private final String[] financialYears = {"2024-25", "2025-26", "2026-27", "2027-28"};
    private final String[] months = {
            "April", "May", "June", "July", "August", "September",
            "October", "November", "December", "January", "February", "March"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Temple Expenses");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();
        setupSpinners();
        setupRecyclerView();
        loadExpenses();
    }

    private void initViews() {
        spExpenseFY = findViewById(R.id.spExpenseFY);
        spExpenseMonth = findViewById(R.id.spExpenseMonth);
        tvTotalExpenseAmount = findViewById(R.id.tvTotalExpenseAmount);
        rvExpenses = findViewById(R.id.rvExpenses);
        fabAddExpense = findViewById(R.id.fabAddExpense);
        fabAddExpense.setOnClickListener(v -> showAddExpenseDialog());
    }

    private void setupSpinners() {
        ArrayAdapter<String> fyAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, financialYears);
        fyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spExpenseFY.setAdapter(fyAdapter);
        spExpenseFY.setSelection(2); // 2026-27

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spExpenseMonth.setAdapter(monthAdapter);
        spExpenseMonth.setSelection(6); // October

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadExpenses();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        };

        spExpenseFY.setOnItemSelectedListener(listener);
        spExpenseMonth.setOnItemSelectedListener(listener);
    }

    private void setupRecyclerView() {
        rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExpenseAdapter(expense -> new AlertDialog.Builder(this)
                .setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete this expense record for "
                        + expense.getDescription() + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteExpense(expense))
                .setNegativeButton("Cancel", null)
                .show());
        rvExpenses.setAdapter(adapter);
    }

    private void loadExpenses() {
        if (spExpenseFY.getSelectedItem() == null || spExpenseMonth.getSelectedItem() == null
                || adapter == null) {
            return;
        }

        String fy = spExpenseFY.getSelectedItem().toString();
        String month = spExpenseMonth.getSelectedItem().toString();
        String path = EXPENSES_PATH + "?financialYear=" + fy + "&month=" + month;

        request("GET", path, null, new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONArray array = extractArray(response);
                    List<Expense> expenses = new ArrayList<>();
                    double total = 0.0;
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject item = array.optJSONObject(i);
                        if (item != null) {
                            Expense expense = parseExpense(item);
                            expenses.add(expense);
                            total += expense.getAmount();
                        }
                    }
                    currentExpenses = expenses;
                    adapter.setExpenses(expenses);

                    NumberFormat inrFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
                    tvTotalExpenseAmount.setText(inrFormat.format(total));
                } catch (Exception e) {
                    showToast("Could not read expenses returned by server.");
                }
            }

            @Override
            public void onError(String message) {
                showToast("Failed to load expenses: " + message);
            }
        });
    }

    private void showAddExpenseDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_expense, null);

        EditText etDesc = dialogView.findViewById(R.id.etExpenseDesc);
        EditText etAmount = dialogView.findViewById(R.id.etExpenseAmount);
        EditText etDate = dialogView.findViewById(R.id.etExpenseDate);
        EditText etRemarks = dialogView.findViewById(R.id.etExpenseRemarks);
        Button btnSave = dialogView.findViewById(R.id.btnExpenseSave);
        Button btnCancel = dialogView.findViewById(R.id.btnExpenseCancel);

        SimpleDateFormat displayFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        etDate.setText(displayFormat.format(Calendar.getInstance().getTime()));

        etDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(ExpenseActivity.this,
                    (view, year, monthOfYear, dayOfMonth) -> {
                        String formatted = String.format(Locale.getDefault(), "%02d-%02d-%04d",
                                dayOfMonth, monthOfYear + 1, year);
                        etDate.setText(formatted);
                    },
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            dpd.show();
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String description = etDesc.getText().toString().trim();
            String amountText = etAmount.getText().toString().trim();
            String dateText = etDate.getText().toString().trim();
            String remarks = etRemarks.getText().toString().trim();

            if (description.isEmpty()) {
                etDesc.setError("Expense description is required");
                return;
            }
            if (amountText.isEmpty()) {
                etAmount.setError("Amount is required");
                return;
            }

            final double amount;
            try {
                amount = Double.parseDouble(amountText);
                if (!Double.isFinite(amount) || amount <= 0) {
                    etAmount.setError("Amount must be greater than zero");
                    return;
                }
            } catch (NumberFormatException e) {
                etAmount.setError("Invalid amount");
                return;
            }

            if (dateText.isEmpty()) {
                etDate.setError("Date is required");
                return;
            }

            final String isoDate;
            try {
                SimpleDateFormat parser = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                parser.setLenient(false);
                Date parsedDate = parser.parse(dateText);
                if (parsedDate == null) throw new ParseException("Invalid date", 0);
                isoDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(parsedDate);
            } catch (ParseException e) {
                etDate.setError("Enter a valid date");
                return;
            }

            String fy = spExpenseFY.getSelectedItem().toString();
            String month = spExpenseMonth.getSelectedItem().toString();

            JSONObject body = new JSONObject();
            try {
                body.put("financialYear", fy);
                body.put("month", month);
                body.put("description", description);
                body.put("amount", amount);
                body.put("expenseDate", isoDate);
                body.put("remarks", remarks);
            } catch (JSONException e) {
                showToast("Could not prepare expense details.");
                return;
            }

            btnSave.setEnabled(false);
            request("POST", EXPENSES_PATH, body, new ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    dialog.dismiss();
                    showToast("Expense recorded successfully");
                    loadExpenses();
                }

                @Override
                public void onError(String message) {
                    btnSave.setEnabled(true);
                    showToast("Could not save expense: " + message);
                }
            });
        });

        dialog.show();
    }

    private void deleteExpense(Expense expense) {
        request("DELETE", EXPENSES_PATH + "/" + expense.getId(), null, new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                showToast("Expense deleted");
                loadExpenses();
            }

            @Override
            public void onError(String message) {
                showToast("Could not delete expense: " + message);
            }
        });
    }

    private Expense parseExpense(JSONObject json) throws JSONException, ParseException {
        String financialYear = json.optString("financialYear",
                json.optString("fy", ""));
        String month = json.optString("month", "");
        String description = json.optString("description", "");
        double amount = json.optDouble("amount", 0.0);
        String isoDate = json.optString("expenseDate", json.optString("date", ""));
        String remarks = json.isNull("remarks") ? "" : json.optString("remarks", "");

        String displayDate = isoDate;
        if (isoDate.length() >= 10 && isoDate.charAt(4) == '-') {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(isoDate.substring(0, 10));
            if (date != null) {
                displayDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date);
            }
        }

        Expense expense = new Expense(financialYear, month, description, amount, displayDate, remarks);
        expense.setId(json.getInt("id"));
        return expense;
    }

    private JSONArray extractArray(String response) throws JSONException {
        Object parsed = new org.json.JSONTokener(response).nextValue();
        if (parsed instanceof JSONArray) {
            return (JSONArray) parsed;
        }
        if (parsed instanceof JSONObject) {
            JSONObject object = (JSONObject) parsed;
            JSONArray data = object.optJSONArray("data");
            if (data != null) return data;
            JSONArray content = object.optJSONArray("content");
            if (content != null) return content;
            JSONArray expenses = object.optJSONArray("expenses");
            if (expenses != null) return expenses;
        }
        throw new JSONException("Expected a JSON array of expenses");
    }

    private interface ApiCallback {
        void onSuccess(String response);
        void onError(String message);
    }

    private void request(String method, String path, JSONObject body, ApiCallback callback) {
        BackendApi.request(this, method, path, body, new BackendApi.Callback() {
            @Override
            public void onSuccess(String response) {
                callback.onSuccess(response);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private void showToast(String message) {
        if (!isFinishing()) {
            runOnUiThread(() ->
                    Toast.makeText(ExpenseActivity.this, message, Toast.LENGTH_LONG).show());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
