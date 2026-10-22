package com.temple.donation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.temple.donation.R;
import com.temple.donation.network.BackendApi;

public class LoginSelectionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BackendApi.isLoggedIn(this)) {
            String role = BackendApi.getRole(this);
            if ("ADMIN".equalsIgnoreCase(role)) {
                openDashboard(MainActivity.class);
                return;
            } else if ("DEVOTEE".equalsIgnoreCase(role)) {
                openDashboard(DevoteeDashboardActivity.class);
                return;
            } else {
                BackendApi.logout(this);
            }
        }

        setContentView(R.layout.activity_login_selection);
        Button btnAdminLogin = findViewById(R.id.btnAdminLogin);
        Button btnDevoteeLogin = findViewById(R.id.btnDevoteeLogin);

        btnAdminLogin.setOnClickListener(v ->
                startActivity(new Intent(this, AdminLoginActivity.class)));
        btnDevoteeLogin.setOnClickListener(v ->
                startActivity(new Intent(this, DevoteeLoginActivity.class)));
    }

    private void openDashboard(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
