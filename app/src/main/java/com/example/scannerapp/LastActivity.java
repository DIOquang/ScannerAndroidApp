package com.example.scannerapp;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LastActivity extends AppCompatActivity {

    private ImageView imageView;
    private Button save;
    private Bitmap currentBitmap;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.last_activity);

        imageView = findViewById(R.id.imageView2);
        save = findViewById(R.id.save);
        currentBitmap = BitmapHolder.getBitmap();
        imageView.setImageBitmap(currentBitmap);
        save.setOnClickListener(v -> saveAsPdf());
    }

    private void saveAsPdf() {
        if (currentBitmap == null) {
            Toast.makeText(this, "XXX", Toast.LENGTH_SHORT).show();
            return;
        }


        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                currentBitmap.getWidth(),
                currentBitmap.getHeight(),
                1
        ).create();

        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        page.getCanvas().drawBitmap(currentBitmap, 0, 0, null);
        pdfDocument.finishPage(page);


        String fileName = "Scan_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) +
                ".pdf";

      //từ dòng dươi là code lưu vaof  thu mục trong máy
        File dir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "Scan2Form");//Tạo đường dẫn
        if (!dir.exists()) dir.mkdirs();//nếu đường dẫn exist thì tạo file mới

        File file = new File(dir, fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            pdfDocument.writeTo(fos);
            pdfDocument.close();
            fos.close();

            Toast.makeText(this,
                    "Đã lưu PDF vào Download/Scan2Form:\n" + fileName,
                    Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this,
                    "Lỗi khi lưu PDF: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
