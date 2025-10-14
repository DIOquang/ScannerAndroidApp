package com.example.scannerapp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.scannerapp.BitmapHolder;
import com.example.scannerapp.R;

import java.io.IOException;

public class MainActivity2 extends AppCompatActivity {

    private ImageView imageView;
    private Bitmap currentBitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // Khởi tạo các view
        imageView = findViewById(R.id.imageView);
        ImageButton confirmButton = findViewById(R.id.imageButton);
        Button cropButton = findViewById(R.id.button);
        Button rotateButton = findViewById(R.id.Rotate);
        Button filterButton = findViewById(R.id.button3);
        Button resetButton = findViewById(R.id.button4);

        // Tải ảnh từ Intent và xử lý
        loadInitialImage();

        // Gán sự kiện cho các nút
        confirmButton.setOnClickListener(v -> {
            // Bitmap đã được cập nhật trong BitmapHolder, chỉ cần chuyển Activity
            startActivity(new Intent(MainActivity2.this, LastActivity.class));
        });

        rotateButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity2.this, RotateActivity.class));
        });

        cropButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity2.this, CropActivity.class));
        });

        filterButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity2.this, FilterActivity.class));
        });

        resetButton.setOnClickListener(v -> {
            Bitmap originalBitmap = BitmapHolder.getOriginalBitmap();
            if (originalBitmap != null) {
                // TẠO BẢN SAO của ảnh gốc để chỉnh sửa, đảm bảo ảnh gốc không đổi
                currentBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                imageView.setImageBitmap(currentBitmap);
                // Cập nhật lại ảnh hiện tại trong Holder
                BitmapHolder.setBitmap(currentBitmap);
            }
        });
    }

    private void loadInitialImage() {
        Uri imageUri = getIntent().getData();

        // KIỂM TRA URI NULL
        if (imageUri == null) {
            Toast.makeText(this, "Không nhận được ảnh", Toast.LENGTH_SHORT).show();
            finish(); // Đóng Activity nếu không có ảnh
            return;
        }

        try {
            Bitmap loadedBitmap;
            // KIỂM TRA PHIÊN BẢN ANDROID
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // Dành cho Android 9+
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imageUri);
                loadedBitmap = ImageDecoder.decodeBitmap(source, (decoder, info, src) -> {
                    decoder.setMutableRequired(true); // Cho phép chỉnh sửa bitmap
                });
            } else {
                // Dành cho các phiên bản Android cũ hơn
                // Bỏ qua dòng bị cảnh báo vì đây là cách làm cho API cũ
                loadedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            }

            // Tạo một bản sao có thể chỉnh sửa để làm ảnh hiện tại
            currentBitmap = loadedBitmap.copy(Bitmap.Config.ARGB_8888, true);

            // Lưu cả ảnh gốc và ảnh hiện tại vào Holder
            BitmapHolder.setOriginalBitmap(loadedBitmap); // Lưu bản gốc để reset
            BitmapHolder.setBitmap(currentBitmap);      // Lưu bản sẽ được chỉnh sửa

            imageView.setImageBitmap(currentBitmap);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi tải ảnh", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Lấy bitmap đã được chỉnh sửa từ các Activity khác (Crop, Rotate,...)
        Bitmap editedBitmap = BitmapHolder.getBitmap();

        // Chỉ cập nhật lại ảnh nếu nó thực sự đã thay đổi
        if (editedBitmap != null && editedBitmap != currentBitmap) {
            currentBitmap = editedBitmap;
            imageView.setImageBitmap(currentBitmap);
        }
    }
}