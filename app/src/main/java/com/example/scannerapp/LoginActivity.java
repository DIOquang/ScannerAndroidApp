package com.example.scannerapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button loginButton = findViewById(R.id.btn_login);

        loginButton.setOnClickListener(v -> {
            // TODO: Thêm logic xác thực đăng nhập thật ở đây (kiểm tra email, password)

            // Giả sử đăng nhập thành công, chuyển sang MainActivity
            Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            // Cờ này sẽ xóa các Activity cũ khỏi stack, người dùng không thể quay lại màn hình đăng nhập
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // Không cần gọi finish() vì cờ trên đã xử lý việc đóng Activity
        });
    }
}