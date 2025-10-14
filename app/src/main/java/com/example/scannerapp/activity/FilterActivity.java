package com.example.scannerapp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.scannerapp.BitmapHolder;
import com.example.scannerapp.R;

import jp.co.cyberagent.android.gpuimage.GPUImageView;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageContrastFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGrayscaleFilter;

public class FilterActivity extends AppCompatActivity {

    private GPUImageView gpuImageView;
    private Button btnGray, btnContrast, btnNone, btnConfirm;
    private SeekBar seekBar;
    private GPUImageFilter currentFilter;
    private Bitmap currentBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter);

        gpuImageView = findViewById(R.id.gpuImageView);
        btnGray = findViewById(R.id.btnGray);
        btnContrast = findViewById(R.id.btnContrast);
        btnNone = findViewById(R.id.btnNone);
        btnConfirm = findViewById(R.id.btnConfirm);
        seekBar = findViewById(R.id.seekBar);


        currentBitmap = BitmapHolder.getBitmap();
        if (currentBitmap != null) {
            gpuImageView.setImage(currentBitmap);
        } else {
            Toast.makeText(this, "XXX", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        btnGray.setOnClickListener(v -> {
            currentFilter = new GPUImageGrayscaleFilter();
            gpuImageView.setFilter(currentFilter);
        });


        btnContrast.setOnClickListener(v -> {
            currentFilter = new GPUImageContrastFilter(1.5f);
            gpuImageView.setFilter(currentFilter);
            seekBar.setProgress(75);
        });


        btnNone.setOnClickListener(v -> {
            currentFilter = new GPUImageFilter();
            gpuImageView.setFilter(currentFilter);
        });


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (currentFilter instanceof GPUImageContrastFilter) {
                    float value = progress / 50f; // 0 ~ 2.0
                    ((GPUImageContrastFilter) currentFilter).setContrast(value);
                    gpuImageView.requestRender();
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });


        btnConfirm.setOnClickListener(v -> {
            try {
                Bitmap result = gpuImageView.capture();
                BitmapHolder.setBitmap(result);

                Intent intent = new Intent(FilterActivity.this, MainActivity2.class);
                startActivity(intent);
                finish();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Toast.makeText(this, "XXX", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
