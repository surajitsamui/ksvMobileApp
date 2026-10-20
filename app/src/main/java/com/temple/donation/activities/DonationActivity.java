package com.temple.donation.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
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

import com.temple.donation.R;
import com.temple.donation.adapters.MonthlyDonationAdapter;
import com.temple.donation.model.Devotee;
import com.temple.donation.model.Donation;
import com.temple.donation.network.BackendApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DonationActivity extends AppCompatActivity {

    private Spinner spFinancialYear, spDevotee, spMonth, spPaymentMode;
    private EditText etDonationAmount, etDonationDate, etRemarks;
    private TextView tvDefaultAmountNotice;
    private Button btnSaveDonation, btnDeleteDonation;
    private RecyclerView rvMonthlyStatus;

    private List<Devotee> devoteeList = new ArrayList<>();
    private List<Donation> yearDonations = new ArrayList<>();
    private MonthlyDonationAdapter monthlyStatusAdapter;
    private Donation existingDonation;
    private boolean devoteesLoaded = false;

    private final String[] financialYears = {"2024-25", "2025-26", "2026-27", "2027-28"};
    private final String[] months = {
            "April", "May", "June", "July", "August", "September",
            "October", "November", "December", "January", "February", "March"
    };
    private final String[] paymentModes = {"Cash", "UPI", "Bank Transfer", "Cheque"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Monthly Donation");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();
        setupSpinners();
        setupDatePicker();
        setupMonthlyRecyclerView();
        setupSaveButton();
        loadDevoteesFromBackend();
    }

    private void initViews() {
        spFinancialYear = findViewById(R.id.spDonationFY);
        spDevotee = findViewById(R.id.spDevotee);
        spMonth = findViewById(R.id.spDonationMonth);
        spPaymentMode = findViewById(R.id.spPaymentMode);
        etDonationAmount = findViewById(R.id.etDonationAmount);
        etDonationDate = findViewById(R.id.etDonationDate);
        etRemarks = findViewById(R.id.etDonationRemarks);
        tvDefaultAmountNotice = findViewById(R.id.tvDefaultAmountNotice);
        btnSaveDonation = findViewById(R.id.btnSaveDonation);
        btnDeleteDonation = findViewById(R.id.btnDeleteDonation);
        rvMonthlyStatus = findViewById(R.id.rvMonthlyStatus);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        etDonationDate.setText(sdf.format(Calendar.getInstance().getTime()));
        setupDeleteButton();
    }

    private void setupSpinners() {
        ArrayAdapter<String> fyAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, financialYears);
        fyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFinancialYear.setAdapter(fyAdapter);
        spFinancialYear.setSelection(2);

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMonth.setAdapter(monthAdapter);
        spMonth.setSelection(6);

        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, paymentModes);
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPaymentMode.setAdapter(paymentAdapter);

        String selectedFy = getIntent().getStringExtra("selected_fy");
        String selectedMonth = getIntent().getStringExtra("selected_month");
        if (selectedFy != null) {
            for (int i = 0; i < financialYears.length; i++) {
                if (financialYears[i].equals(selectedFy)) {
                    spFinancialYear.setSelection(i);
                    break;
                }
            }
        }
        if (selectedMonth != null) {
            for (int i = 0; i < months.length; i++) {
                if (months[i].equalsIgnoreCase(selectedMonth)) {
                    spMonth.setSelection(i);
                    break;
                }
            }
        }

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (devoteesLoaded) {
                    onDevoteeOrFYSelected();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        };
        spDevotee.setOnItemSelectedListener(listener);
        spFinancialYear.setOnItemSelectedListener(listener);

        spMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (devoteesLoaded) {
                    checkExistingDonationForMonth();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void loadDevoteesFromBackend() {
        BackendApi.get(this, "/api/admin/devotees?includeInactive=false", new BackendApi.Callback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONArray array = extractArray(response, "devotees");
                    List<Devotee> loaded = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject item = array.optJSONObject(i);
                        if (item != null) {
                            loaded.add(parseDevotee(item));
                        }
                    }

                    devoteeList = loaded;
                    ArrayAdapter<Devotee> devoteeAdapter = new ArrayAdapter<>(
                            DonationActivity.this,
                            android.R.layout.simple_spinner_item,
                            devoteeList);
                    devoteeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spDevotee.setAdapter(devoteeAdapter);

                    long preselectedId = getIntent().getLongExtra("selected_devotee_id", -1);
                    if (preselectedId != -1) {
                        for (int i = 0; i < devoteeList.size(); i++) {
                            if (devoteeList.get(i).getId() == preselectedId) {
                                spDevotee.setSelection(i);
                                break;
                            }
                        }
                    }

                    devoteesLoaded = true;
                    if (devoteeList.isEmpty()) {
                        tvDefaultAmountNotice.setText("No active devotees found.");
                        btnSaveDonation.setEnabled(false);
                    } else {
                        btnSaveDonation.setEnabled(true);
                        onDevoteeOrFYSelected();
                    }
                } catch (Exception e) {
                    showToast("Could not parse devotees from backend.");
                }
            }

            @Override
            public void onError(String message) {
                showToast("Failed to load devotees: " + message);
            }
        });
    }

    private void onDevoteeOrFYSelected() {
        Devotee selected = selectedDevotee();
        if (selected == null) return;

        tvDefaultAmountNotice.setText("Default monthly amount: ₹"
                + (int) selected.getMonthlyAmount()
                + " (Master amount remains unchanged)");
        checkExistingDonationForMonth();
        refreshMonthlyStatusList();
    }

    private void checkExistingDonationForMonth() {
        Devotee selected = selectedDevotee();
        if (selected == null || spFinancialYear.getSelectedItem() == null
                || spMonth.getSelectedItem() == null) {
            return;
        }

        String fy = selectedFinancialYear();
        String month = selectedMonth();
        String path = "/api/admin/donations?devoteeId=" + selected.getId()
                + "&financialYear=" + fy;

        BackendApi.get(this, path, new BackendApi.Callback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONArray array = extractArray(response, "donations");
                    Donation found = null;
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject item = array.optJSONObject(i);
                        if (item != null && month.equalsIgnoreCase(item.optString("month", ""))) {
                            found = parseDonation(item, (int) selected.getId(), fy);
                            break;
                        }
                    }
                    existingDonation = found;
                    displayExistingDonation(found, selected);
                } catch (Exception e) {
                    showToast("Could not parse donation records.");
                }
            }

            @Override
            public void onError(String message) {
                showToast("Failed to check donation: " + message);
            }
        });
    }

    private void displayExistingDonation(Donation donation, Devotee selected) {
        if (donation != null) {
            etDonationAmount.setText(String.valueOf(donation.getAmount()));
            etDonationDate.setText(donation.getDonationDate());
            etRemarks.setText(donation.getRemarks() == null ? "" : donation.getRemarks());

            for (int i = 0; i < paymentModes.length; i++) {
                if (paymentModes[i].equalsIgnoreCase(donation.getPaymentMode())) {
                    spPaymentMode.setSelection(i);
                    break;
                }
            }
            btnSaveDonation.setText("Update Donation (Paid)");
            if (btnDeleteDonation != null) btnDeleteDonation.setVisibility(View.VISIBLE);
        } else {
            etDonationAmount.setText(String.valueOf((int) selected.getMonthlyAmount()));
            etRemarks.setText("");
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            etDonationDate.setText(sdf.format(Calendar.getInstance().getTime()));
            btnSaveDonation.setText("Save Donation");
            if (btnDeleteDonation != null) btnDeleteDonation.setVisibility(View.GONE);
        }
    }

    private void setupDeleteButton() {
        if (btnDeleteDonation == null) return;
        btnDeleteDonation.setOnClickListener(v -> {
            Devotee selected = selectedDevotee();
            Donation donation = existingDonation;
            if (selected == null || donation == null) {
                showToast("No saved donation record selected.");
                return;
            }

            String month = selectedMonth();
            String fy = selectedFinancialYear();
            new AlertDialog.Builder(this)
                    .setTitle("Delete Donation Record")
                    .setMessage("Delete the " + month + " " + fy + " donation record of ₹"
                            + (int) donation.getAmount() + " for " + selected.getName()
                            + "? The status for this month will revert to Pending.")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Delete Record", (dialog, which) ->
                            BackendApi.delete(this, "/api/admin/donations/" + donation.getId(),
                                    new BackendApi.Callback() {
                                        @Override
                                        public void onSuccess(String response) {
                                            showToast("Donation record deleted");
                                            existingDonation = null;
                                            refreshMonthlyStatusList();
                                            checkExistingDonationForMonth();
                                        }

                                        @Override
                                        public void onError(String message) {
                                            showToast("Failed to delete donation: " + message);
                                        }
                                    }))
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void setupDatePicker() {
        etDonationDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(DonationActivity.this,
                    (view, year, monthOfYear, dayOfMonth) -> {
                        String formatted = String.format(Locale.getDefault(), "%02d-%02d-%04d",
                                dayOfMonth, monthOfYear + 1, year);
                        etDonationDate.setText(formatted);
                    },
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            dpd.show();
        });
    }

    private void setupMonthlyRecyclerView() {
        rvMonthlyStatus.setLayoutManager(new LinearLayoutManager(this));
        monthlyStatusAdapter = new MonthlyDonationAdapter(monthName -> {
            for (int i = 0; i < months.length; i++) {
                if (months[i].equalsIgnoreCase(monthName)) {
                    spMonth.setSelection(i);
                    break;
                }
            }
        });
        rvMonthlyStatus.setAdapter(monthlyStatusAdapter);
    }

    private void refreshMonthlyStatusList() {
        Devotee selected = selectedDevotee();
        if (selected == null || spFinancialYear.getSelectedItem() == null) return;

        String fy = selectedFinancialYear();
        monthlyStatusAdapter.setMessageDetails(selected, fy);

        BackendApi.get(this, "/api/admin/donations?devoteeId=" + selected.getId()
                + "&financialYear=" + fy, new BackendApi.Callback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONArray array = extractArray(response, "donations");
                    List<Donation> loaded = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject item = array.optJSONObject(i);
                        if (item != null) {
                            loaded.add(parseDonation(item, (int) selected.getId(), fy));
                        }
                    }
                    yearDonations = loaded;
                    monthlyStatusAdapter.updateData(
                            months, yearDonations, selected.getMonthlyAmount());
                } catch (Exception e) {
                    showToast("Could not parse yearly donation status.");
                }
            }

            @Override
            public void onError(String message) {
                showToast("Failed to load donation status: " + message);
            }
        });
    }

    private void setupSaveButton() {
        btnSaveDonation.setOnClickListener(v -> {
            Devotee selected = selectedDevotee();
            if (selected == null) {
                showToast("Please select a devotee");
                return;
            }

            String fy = selectedFinancialYear();
            String month = selectedMonth();
            String amountText = etDonationAmount.getText().toString().trim();
            String dateText = etDonationDate.getText().toString().trim();
            String mode = spPaymentMode.getSelectedItem() == null
                    ? "Cash" : spPaymentMode.getSelectedItem().toString();
            String remarks = etRemarks.getText().toString().trim();

            if (amountText.isEmpty()) {
                etDonationAmount.setError("Please enter amount");
                return;
            }

            final double amount;
            try {
                amount = Double.parseDouble(amountText);
                if (!Double.isFinite(amount) || amount <= 0) {
                    etDonationAmount.setError("Amount must be greater than zero");
                    return;
                }
            } catch (NumberFormatException e) {
                etDonationAmount.setError("Invalid amount");
                return;
            }

            final String isoDate;
            try {
                SimpleDateFormat parser = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                parser.setLenient(false);
                Date date = parser.parse(dateText);
                if (date == null) throw new ParseException("Invalid date", 0);
                isoDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date);
            } catch (Exception e) {
                etDonationDate.setError("Enter a valid date");
                return;
            }

            JSONObject body = new JSONObject();
            try {
                body.put("devoteeId", selected.getId());
                body.put("financialYear", fy);
                body.put("month", month);
                body.put("amount", amount);
                body.put("donationDate", isoDate);
                body.put("paymentMode", mode);
                body.put("remarks", remarks);
            } catch (JSONException e) {
                showToast("Could not prepare donation details.");
                return;
            }

            btnSaveDonation.setEnabled(false);
            String method = existingDonation == null ? "POST" : "PUT";
            String path = existingDonation == null
                    ? "/api/admin/donations"
                    : "/api/admin/donations/" + existingDonation.getId();

            BackendApi.request(this, method, path, body, new BackendApi.Callback() {
                @Override
                public void onSuccess(String response) {
                    btnSaveDonation.setEnabled(true);
                    showToast("Donation of ₹" + amount + " for " + month + " saved!");
                    checkExistingDonationForMonth();
                    refreshMonthlyStatusList();
                }

                @Override
                public void onError(String message) {
                    btnSaveDonation.setEnabled(true);
                    showToast("Failed to save donation: " + message);
                }
            });
        });
    }

    private Devotee selectedDevotee() {
        Object item = spDevotee.getSelectedItem();
        return item instanceof Devotee ? (Devotee) item : null;
    }

    private String selectedFinancialYear() {
        return spFinancialYear.getSelectedItem() == null
                ? financialYears[2] : spFinancialYear.getSelectedItem().toString();
    }

    private String selectedMonth() {
        return spMonth.getSelectedItem() == null
                ? months[6] : spMonth.getSelectedItem().toString();
    }

    private Devotee parseDevotee(JSONObject json) throws JSONException {
        Devotee devotee = new Devotee(
                json.optString("name", ""),
                json.isNull("mobile") ? "" : json.optString("mobile", ""),
                json.isNull("address") ? "" : json.optString("address", ""),
                json.optDouble("monthlyAmount", 0.0),
                json.optBoolean("active", true));
        devotee.setId(json.getInt("id"));
        return devotee;
    }

    private Donation parseDonation(JSONObject json, int fallbackDevoteeId, String fallbackFy)
            throws JSONException {
        int devoteeId = json.optInt("devoteeId", fallbackDevoteeId);
        String fy = json.optString("financialYear", fallbackFy);
        String month = json.optString("month", "");
        double amount = json.optDouble("amount", 0.0);
        String date = json.optString("donationDate", "");
        if (date.length() >= 10 && date.charAt(4) == '-') {
            try {
                Date parsed = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date.substring(0, 10));
                if (parsed != null) {
                    date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(parsed);
                }
            } catch (ParseException ignored) { }
        }
        String paymentMode = json.optString("paymentMode", "Cash");
        String remarks = json.isNull("remarks") ? "" : json.optString("remarks", "");

        Donation donation = new Donation(devoteeId, fy, month, amount, date, paymentMode, remarks);
        if (json.has("id")) donation.setId(json.getInt("id"));
        return donation;
    }

    private JSONArray extractArray(String response, String wrapperKey) throws JSONException {
        Object parsed = new org.json.JSONTokener(response).nextValue();
        if (parsed instanceof JSONArray) return (JSONArray) parsed;
        if (parsed instanceof JSONObject) {
            JSONObject object = (JSONObject) parsed;
            JSONArray wrapped = object.optJSONArray(wrapperKey);
            if (wrapped != null) return wrapped;
            wrapped = object.optJSONArray("data");
            if (wrapped != null) return wrapped;
            wrapped = object.optJSONArray("content");
            if (wrapped != null) return wrapped;
        }
        throw new JSONException("Expected JSON array");
    }

    private void showToast(String message) {
        if (!isFinishing()) {
            runOnUiThread(() -> Toast.makeText(
                    DonationActivity.this, message, Toast.LENGTH_LONG).show());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
