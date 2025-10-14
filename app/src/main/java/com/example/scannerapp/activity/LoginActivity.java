package com.example.scannerapp.activity;

import android.content.Intent;
import android.content.SharedPreferences; // THÊM import này
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.scannerapp.DatabaseHelper;
import com.example.scannerapp.R;

public class LoginActivity extends AppCompatActivity {

    public static final String SHARED_PREFS = "ScannerAppPrefs";
    public static final String LOGGED_IN_USER_EMAIL = "loggedInUserEmail";

    private DatabaseHelper databaseHelper;
    private EditText emailEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        databaseHelper = new DatabaseHelper(this);
        emailEditText = findViewById(R.id.edit_text_email);
        passwordEditText = findViewById(R.id.edit_text_password);
        Button loginButton = findViewById(R.id.btn_login);
        TextView registerLink = findViewById(R.id.text_view_register_link);

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (databaseHelper.checkUser(email, password)) {

                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(LOGGED_IN_USER_EMAIL, email);
                editor.apply(); // Lưu lại thông tin

                Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                goToMainActivity(email);
            } else {
                Toast.makeText(LoginActivity.this, "Email hoặc mật khẩu không đúng!", Toast.LENGTH_SHORT).show();
            }
        });

        registerLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void goToMainActivity(String userEmail) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("USER_EMAIL", userEmail);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}