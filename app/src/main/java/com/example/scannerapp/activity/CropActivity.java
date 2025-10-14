package com.example.scannerapp.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.canhub.cropper.CropImageView;
import com.example.scannerapp.BitmapHolder;
import com.example.scannerapp.R;

public class CropActivity extends AppCompatActivity {

    private CropImageView cropImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crop);

        cropImageView = findViewById(R.id.cropImageView);
        ImageButton Done = findViewById(R.id.imageButtonDone);
        ImageButton Cancel = findViewById(R.id.imageButtonCancel);


        cropImageView.setImageBitmap(BitmapHolder.getBitmap());
        cropImageView.setGuidelines(CropImageView.Guidelines.ON);


        Done.setOnClickListener(v -> {
            Bitmap editedBitmap = cropImageView.getCroppedImage();
            if (editedBitmap != null) {
                BitmapHolder.setBitmap(editedBitmap);
            }
            finish();
        });


        Cancel.setOnClickListener(v -> finish());
    }
}
