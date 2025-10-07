package com.example.scannerapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RotateActivity extends AppCompatActivity {

    private ImageView imageView;
    private ImageButton rotate;
    private ImageButton confirm;
    private Bitmap currentBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rotate);

        imageView = findViewById(R.id.imageView3);
        rotate = findViewById(R.id.imageButton2);
        confirm =findViewById(R.id.imageButton3);

        currentBitmap = BitmapHolder.getBitmap();
        if(currentBitmap != null){
            imageView.setImageBitmap(currentBitmap);
        }
        else {
            Toast.makeText(this,"XXX",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        rotate.setOnClickListener(v -> {
            if (currentBitmap != null) {
                currentBitmap = rotateBitmap(currentBitmap, 90);
                imageView.setImageBitmap(currentBitmap);
            }
        });
        confirm.setOnClickListener(v-> {
            BitmapHolder.setBitmap(currentBitmap);
            Intent intent = new Intent(RotateActivity.this,MainActivity2.class);
            finish();
        });
    }

    private Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(
                source, 0, 0,
                source.getWidth(), source.getHeight(),
                matrix, true
        );
    }
}
