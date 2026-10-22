package com.temple.donation.activities;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.temple.donation.network.BackendApi;

/**
 * Basic landing screen for Devotee accounts.
 * Replace/add personal donation API calls when the backend endpoint and response schema are confirmed.
 */
public class DevoteeDashboardActivity extends AppCompatActivity {
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

        int pad = (int) (24 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(pad, pad, pad, pad);
        root.setBackgroundColor(0xFFF4F7FC);

        TextView title = new TextView(this);
        title.setText("Devotee Dashboard");
        title.setTextSize(26);
        title.setTextColor(0xFF173B70);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView message = new TextView(this);
        message.setText("You are signed in as a Devotee. Personal donation status will appear here once the backend devotee endpoint is connected.");
        message.setTextSize(16);
        message.setGravity(Gravity.CENTER);
        message.setPadding(0, pad, 0, pad);
        root.addView(message, new LinearLayout.LayoutParams(-1, -2));

        TextView logout = new TextView(this);
        logout.setText("Log out");
        logout.setTextSize(16);
        logout.setTextColor(0xFF2457A7);
        logout.setGravity(Gravity.CENTER);
        logout.setPadding(pad, pad, pad, pad);
        root.addView(logout, new LinearLayout.LayoutParams(-1, -2));
        logout.setOnClickListener(v -> {
            BackendApi.logout(this);
            android.content.Intent intent = new android.content.Intent(this, LoginSelectionActivity.class);
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                    | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        setContentView(root);
    }
}
