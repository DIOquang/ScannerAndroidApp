package com.example.scannerapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2000 milliseconds = 2 giây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Sử dụng Handler để trì hoãn việc chuyển sang LoginActivity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Tạo một Intent để mở LoginActivity
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);

            // Đóng SplashActivity để người dùng không thể quay lại màn hình này
            finish();
        }, SPLASH_DELAY);
    }
}