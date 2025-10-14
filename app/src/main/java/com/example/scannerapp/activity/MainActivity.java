package com.example.scannerapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.scannerapp.fragment.ConvertFragment;
import com.example.scannerapp.fragment.FormFragment;
import com.example.scannerapp.R;
import com.example.scannerapp.fragment.ScanFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        TextView welcomeTextView = findViewById(R.id.text_view_welcome);
        Button logoutButton = findViewById(R.id.btn_logout);

        // 1. Nhận email từ LoginActivity
        Intent intent = getIntent();
        String userEmail = intent.getStringExtra("USER_EMAIL");

        // 2. Hiển thị lời chào
//        if (userEmail != null && !userEmail.isEmpty()) {
//            welcomeTextView.setText("Wellcome!!" + userEmail);
//        }

        // 3. Xử lý sự kiện đăng xuất
        logoutButton.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            Intent logoutIntent = new Intent(MainActivity.this, LoginActivity.class);
            logoutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(logoutIntent);
            finish();
        });

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