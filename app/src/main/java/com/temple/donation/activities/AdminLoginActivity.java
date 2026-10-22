package com.temple.donation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.temple.donation.network.BackendApi;

import org.json.JSONObject;

public class AdminLoginActivity extends AppCompatActivity {
    private EditText emailInput, passwordInput;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildScreen();
        loginButton.setOnClickListener(v -> login());
    }

    private void buildScreen() {
        int pad = (int) (24 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(pad, pad, pad, pad);
        root.setBackgroundColor(0xFFF4F7FC);

        TextView title = new TextView(this);
        title.setText("Admin Login");
        title.setTextSize(26);
        title.setTextColor(0xFF173B70);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        emailInput = new EditText(this);
        emailInput.setHint("Admin email");
        emailInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        root.addView(emailInput, fieldParams(pad));

        passwordInput = new EditText(this);
        passwordInput.setHint("Password");
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        root.addView(passwordInput, fieldParams(pad));

        loginButton = new Button(this);
        loginButton.setText("Sign in as Admin");
        root.addView(loginButton, fieldParams(pad));

        setContentView(root);
    }

    private LinearLayout.LayoutParams fieldParams(int pad) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.topMargin = pad / 2;
        return p;
    }

    private void login() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        loginButton.setEnabled(false);
        BackendApi.login(this, email, password, new BackendApi.Callback() {
            @Override public void onSuccess(String response) {
                loginButton.setEnabled(true);
                String role = BackendApi.getRole(AdminLoginActivity.this);
                if (!"ADMIN".equalsIgnoreCase(role)) {
                    BackendApi.logout(AdminLoginActivity.this);
                    Toast.makeText(AdminLoginActivity.this,
                            "This account is not an Admin account.", Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent(AdminLoginActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override public void onError(String message) {
                loginButton.setEnabled(true);
                Toast.makeText(AdminLoginActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
