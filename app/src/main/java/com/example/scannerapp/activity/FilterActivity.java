package com.example.scannerapp.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.scannerapp.BitmapHolder;
import com.example.scannerapp .R;


import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageView;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageBrightnessFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageContrastFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGrayscaleFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageLuminanceThresholdFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilterGroup;

public class FilterActivity extends AppCompatActivity {

    private GPUImageView gpuImageView;
    private GPUImageFilterGroup filterGroup;

    private SeekBar seekBar;
    private Button gray, brightness, contrast, threshold;
    private ImageButton reset, confirm;

    private Bitmap currentBitmap, filteredBitmap, originalBitmap;

    private GPUImageBrightnessFilter brightnessFilter;
    private GPUImageContrastFilter contrastFilter;
    private GPUImageLuminanceThresholdFilter thresholdFilter;

    private enum CurrentFilter {
        NONE(false),
        GRAY(false),
        BRIGHTNESS(true),
        CONTRAST(true),
        THRESHOLD(false);
        boolean useSeekBar;
        CurrentFilter(boolean useSeekBar){
            this.useSeekBar=useSeekBar;
        }
    }
    private CurrentFilter currentFilter = CurrentFilter.NONE;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter);

        gpuImageView = findViewById(R.id.GpuImageView);

        seekBar = findViewById(R.id.seekBar);
        seekBarBehavior();

        gray = findViewById(R.id.button2);
        brightness = findViewById(R.id.button5);
        contrast = findViewById(R.id.button6);
        threshold = findViewById(R.id.button7);

        reset = findViewById(R.id.imageButton4);
        confirm = findViewById(R.id.imageButton5);

        currentBitmap = BitmapHolder.getBitmap();

        originalBitmap = currentBitmap.copy(currentBitmap.getConfig(), true);
        gpuImageView.getGPUImage().setScaleType(GPUImage.ScaleType.CENTER_INSIDE);

        filterGroup = new GPUImageFilterGroup();
        gpuImageView.setImage(currentBitmap);
        gpuImageView.requestRender();

        gray.setOnClickListener(v -> {
            filterGroup.addFilter(new GPUImageGrayscaleFilter());
            gpuImageView.setFilter(filterGroup);
            gpuImageView.requestRender();
            currentFilter = CurrentFilter.GRAY;
            seekBarBehavior();
        });


        brightness.setOnClickListener(v -> {
            brightnessFilter = new GPUImageBrightnessFilter();
            filterGroup.addFilter(brightnessFilter);
            gpuImageView.setFilter(filterGroup);
            gpuImageView.requestRender();
            currentFilter = CurrentFilter.BRIGHTNESS;
            seekBarBehavior();
        });


        contrast.setOnClickListener(v -> {
            contrastFilter = new GPUImageContrastFilter();
            filterGroup.addFilter(contrastFilter);
            gpuImageView.setFilter(filterGroup);

            gpuImageView.requestRender();

            currentFilter = CurrentFilter.CONTRAST;
            seekBarBehavior();
        });

        threshold.setOnClickListener(v -> {

            thresholdFilter = new GPUImageLuminanceThresholdFilter();
            filterGroup.addFilter(thresholdFilter);
            gpuImageView.setFilter(filterGroup);

            gpuImageView.requestRender();
            currentFilter = CurrentFilter.THRESHOLD;
            seekBarBehavior();
        });

        reset.setOnClickListener(v -> {
            gpuImageView.setFilter(new jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter());

            gpuImageView.setImage(originalBitmap);

            gpuImageView.requestRender();


            filterGroup = new jp.co.cyberagent.android.gpuimage.filter.GPUImageFilterGroup();

            currentFilter = CurrentFilter.NONE;

            seekBarBehavior();

        });


        confirm.setOnClickListener(v -> {
            try{
                filteredBitmap= gpuImageView.capture();
                BitmapHolder.setBitmap(filteredBitmap);
                finish();
            }catch (InterruptedException e){}//@@
        });



        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;

                // Brightness (-1 → +1)
                if (currentFilter == CurrentFilter.BRIGHTNESS && brightnessFilter != null) {
                    float value = (progress - 50f) / 50f;
                    brightnessFilter.setBrightness(value);
                    gpuImageView.requestRender();
                }

                // Contrast (0 → 2)
                if (currentFilter == CurrentFilter.CONTRAST && contrastFilter != null) {
                    float value = progress / 50f;
                    contrastFilter.setContrast(value);
                    gpuImageView.requestRender();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

    }
    private void seekBarBehavior() {
        seekBar.setVisibility(currentFilter.useSeekBar ? View.VISIBLE : View.GONE);
        seekBar.setProgress(50);
    }
}