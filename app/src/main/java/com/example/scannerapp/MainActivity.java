package com.example.scannerapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.scannerapp.LoginActivity; // Dòng import này bây giờ sẽ hoạt động

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- PHẦN CODE MỚI ĐỂ XỬ LÝ ĐĂNG XUẤT ---
        TextView welcomeTextView = findViewById(R.id.text_view_welcome);
        Button logoutButton = findViewById(R.id.btn_logout);

        // 1. Nhận email từ LoginActivity
        Intent intent = getIntent();
        String userEmail = intent.getStringExtra("USER_EMAIL");

        // 2. Hiển thị lời chào
        if (userEmail != null && !userEmail.isEmpty()) {
            welcomeTextView.setText("Wellcome!!\n" + userEmail);
        }

        // 3. Xử lý sự kiện đăng xuất
        logoutButton.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            Intent logoutIntent = new Intent(MainActivity.this, LoginActivity.class);
            logoutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(logoutIntent);
            finish();
        });
        // --- KẾT THÚC PHẦN CODE MỚI ---


        // --- PHẦN CODE CŨ CỦA BẠN (GIỮ NGUYÊN) ---
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ScanFragment())
                    .commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_scan) {
                selectedFragment = new ScanFragment();
            } else if (id == R.id.nav_convert) {
                selectedFragment = new ConvertFragment();
            } else if (id == R.id.nav_form) {
                selectedFragment = new FormFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });
    }
}