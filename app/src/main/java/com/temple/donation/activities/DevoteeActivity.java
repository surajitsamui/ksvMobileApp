package com.temple.donation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.temple.donation.R;
import com.temple.donation.adapters.DevoteeAdapter;
import com.temple.donation.model.Devotee;
import com.temple.donation.network.BackendApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Admin devotee management backed by the Spring Boot REST API.
 *
 * API paths expected:
 * GET    /api/admin/devotees?includeInactive=true
 * POST   /api/admin/devotees
 * PUT    /api/admin/devotees/{id}
 * PATCH  /api/admin/devotees/{id}/status   body: {"active":true|false}
 * DELETE /api/admin/devotees/{id}
 *
 * Update BASE_URL to match the host/port where your Spring Boot backend runs.
 * For the Android emulator, 10.0.2.2 points to the development computer.
 */
public class DevoteeActivity extends AppCompatActivity {
    private static final String DEVOTEES_PATH = "/api/admin/devotees";

    private RecyclerView rvDevotees;
    private EditText etSearchDevotee;
    private FloatingActionButton fabAddDevotee;
    private DevoteeAdapter adapter;
    private List<Devotee> allDevotees = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devotee);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Manage Devotees");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();
        setupRecyclerView();
        setupSearch();
        loadDevotees();
    }

    private void initViews() {
        rvDevotees = findViewById(R.id.rvDevotees);
        etSearchDevotee = findViewById(R.id.etSearchDevotee);
        fabAddDevotee = findViewById(R.id.fabAddDevotee);
        fabAddDevotee.setOnClickListener(v -> showAddOrEditDevoteeDialog(null));
    }

    private void setupRecyclerView() {
        rvDevotees.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DevoteeAdapter(new DevoteeAdapter.OnDevoteeActionListener() {
            @Override
            public void onEdit(Devotee devotee) {
                showAddOrEditDevoteeDialog(devotee);
            }

            @Override
            public void onToggleStatus(Devotee devotee) {
                updateDevoteeStatus(devotee, !devotee.isActive());
            }

            @Override
            public void onViewDonations(Devotee devotee) {
                Intent intent = new Intent(DevoteeActivity.this, DonationActivity.class);
                intent.putExtra("selected_devotee_id", devotee.getId());
                startActivity(intent);
            }

            @Override
            public void onDelete(Devotee devotee) {
                new AlertDialog.Builder(DevoteeActivity.this)
                        .setTitle("Delete Devotee")
                        .setMessage("Are you sure you want to permanently delete devotee \""
                                + devotee.getName()
                                + "\"? Associated donation records may also be affected. This cannot be undone.")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("Delete Devotee", (dialog, which) -> deleteDevotee(devotee))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
        rvDevotees.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearchDevotee.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDevotees(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void loadDevotees() {
        request("GET", DEVOTEES_PATH + "?includeInactive=true", null, new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONArray array = extractArray(response);
                    List<Devotee> loaded = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject json = array.optJSONObject(i);
                        if (json != null) {
                            loaded.add(toDevotee(json));
                        }
                    }
                    allDevotees = loaded;
                    filterDevotees(etSearchDevotee.getText().toString());
                } catch (Exception e) {
                    showToast("Could not read devotees returned by server.");
                }
            }

            @Override
            public void onError(String message) {
                showToast("Failed to load devotees: " + message);
            }
        });
    }

    private void filterDevotees(String query) {
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (normalizedQuery.isEmpty()) {
            adapter.setDevotees(new ArrayList<>(allDevotees));
            return;
        }

        List<Devotee> filtered = new ArrayList<>();
        for (Devotee devotee : allDevotees) {
            String name = safe(devotee.getName()).toLowerCase(Locale.ROOT);
            String mobile = safe(devotee.getMobile());
            String address = safe(devotee.getAddress()).toLowerCase(Locale.ROOT);
            if (name.contains(normalizedQuery)
                    || mobile.contains(normalizedQuery)
                    || address.contains(normalizedQuery)) {
                filtered.add(devotee);
            }
        }
        adapter.setDevotees(filtered);
    }

    private void showAddOrEditDevoteeDialog(Devotee existingDevotee) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_devotee, null);

        EditText etName = dialogView.findViewById(R.id.etDialogName);
        EditText etMobile = dialogView.findViewById(R.id.etDialogMobile);
        EditText etAddress = dialogView.findViewById(R.id.etDialogAddress);
        EditText etAmount = dialogView.findViewById(R.id.etDialogAmount);
        CheckBox cbActive = dialogView.findViewById(R.id.cbDialogActive);
        Button btnSave = dialogView.findViewById(R.id.btnDialogSave);
        Button btnCancel = dialogView.findViewById(R.id.btnDialogCancel);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (existingDevotee != null) {
            dialog.setTitle("Edit Devotee");
            etName.setText(existingDevotee.getName());
            etMobile.setText(existingDevotee.getMobile());
            etAddress.setText(existingDevotee.getAddress());
            etAmount.setText(String.valueOf(existingDevotee.getMonthlyAmount()));
            cbActive.setChecked(existingDevotee.isActive());
        } else {
            dialog.setTitle("Add New Devotee");
            cbActive.setChecked(true);
            etAmount.setText("500");
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String mobile = etMobile.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String amountText = etAmount.getText().toString().trim();

            if (name.isEmpty()) {
                etName.setError("Devotee name is required");
                return;
            }
            if (amountText.isEmpty()) {
                etAmount.setError("Default monthly amount is required");
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
                etAmount.setError("Please enter a valid amount");
                return;
            }

            JSONObject body = new JSONObject();
            try {
                body.put("name", name);
                body.put("mobile", mobile);
                body.put("address", address);
                body.put("monthlyAmount", amount);
                body.put("active", cbActive.isChecked());
            } catch (JSONException e) {
                showToast("Could not prepare devotee details.");
                return;
            }

            String method = existingDevotee == null ? "POST" : "PUT";
            String path = existingDevotee == null
                    ? DEVOTEES_PATH
                    : DEVOTEES_PATH + "/" + existingDevotee.getId();

            btnSave.setEnabled(false);
            request(method, path, body, new ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    dialog.dismiss();
                    showToast(existingDevotee == null
                            ? "Devotee added successfully"
                            : "Devotee updated successfully");
                    loadDevotees();
                }

                @Override
                public void onError(String message) {
                    btnSave.setEnabled(true);
                    showToast("Could not save devotee: " + message);
                }
            });
        });

        dialog.show();
    }

    private void updateDevoteeStatus(Devotee devotee, boolean active) {
        JSONObject body = new JSONObject();
        try {
            body.put("active", active);
        } catch (JSONException e) {
            showToast("Could not prepare status update.");
            return;
        }

        request("PATCH", DEVOTEES_PATH + "/" + devotee.getId() + "/status", body,
                new ApiCallback() {
                    @Override
                    public void onSuccess(String response) {
                        showToast(devotee.getName() + " marked as "
                                + (active ? "Active" : "Inactive"));
                        loadDevotees();
                    }

                    @Override
                    public void onError(String message) {
                        showToast("Could not update status: " + message);
                    }
                });
    }

    private void deleteDevotee(Devotee devotee) {
        request("DELETE", DEVOTEES_PATH + "/" + devotee.getId(), null, new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                showToast("Devotee deleted successfully");
                loadDevotees();
            }

            @Override
            public void onError(String message) {
                showToast("Could not delete devotee: " + message);
            }
        });
    }

    private Devotee toDevotee(JSONObject json) throws JSONException {
        String name = json.optString("name", "");
        String mobile = json.isNull("mobile") ? "" : json.optString("mobile", "");
        String address = json.isNull("address") ? "" : json.optString("address", "");
        double monthlyAmount = json.optDouble("monthlyAmount", 0.0);
        boolean active = json.optBoolean("active", true);

        Devotee devotee = new Devotee(name, mobile, address, monthlyAmount, active);
        devotee.setId(json.getInt("id"));
        return devotee;
    }

    private JSONArray extractArray(String response) throws JSONException {
        Object parsed = new org.json.JSONTokener(response).nextValue();
        if (parsed instanceof JSONArray) {
            return (JSONArray) parsed;
        }
        if (parsed instanceof JSONObject) {
            JSONObject object = (JSONObject) parsed;
            // Supports APIs returning { "data": [...] } or { "content": [...] }.
            JSONArray data = object.optJSONArray("data");
            if (data != null) return data;
            JSONArray content = object.optJSONArray("content");
            if (content != null) return content;
            JSONArray devotees = object.optJSONArray("devotees");
            if (devotees != null) return devotees;
        }
        throw new JSONException("Expected a JSON array of devotees");
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

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void showToast(String message) {
        if (!isFinishing()) {
            runOnUiThread(() ->
                    Toast.makeText(DevoteeActivity.this, message, Toast.LENGTH_LONG).show());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
