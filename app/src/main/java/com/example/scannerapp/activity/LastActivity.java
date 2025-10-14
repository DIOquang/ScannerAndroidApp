package com.example.scannerapp.activity;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.scannerapp.BitmapHolder;
import com.example.scannerapp.R;

import java.io.IOException;
import java.io.OutputStream;
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

        if (currentBitmap != null) {
            imageView.setImageBitmap(currentBitmap);
        }

        save.setOnClickListener(v -> saveAsPdf());
    }

    private void saveAsPdf() {
        if (currentBitmap == null) {
            Toast.makeText(this, "Không có ảnh để lưu.", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences(LoginActivity.SHARED_PREFS, MODE_PRIVATE);
        String userEmail = prefs.getString(LoginActivity.LOGGED_IN_USER_EMAIL, null);

        if (userEmail == null) {
            Toast.makeText(this, "Lỗi: Người dùng chưa đăng nhập.", Toast.LENGTH_SHORT).show();
            return;
        }

        String folderKey = "folderUri_" + userEmail;
        String folderUriString = prefs.getString(folderKey, null);

        if (folderUriString == null) {
            Toast.makeText(this, "Vui lòng chọn thư mục lưu file trong màn hình Scan trước.", Toast.LENGTH_LONG).show();
            return;
        }

        Uri folderUri = Uri.parse(folderUriString); // Đây là "chìa khóa chùm" (Tree URI)

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

        ContentResolver resolver = getContentResolver();
        try {
            Uri parentDocumentUri = DocumentsContract.buildDocumentUriUsingTree(folderUri,
                    DocumentsContract.getTreeDocumentId(folderUri));

            Uri newFileUri = DocumentsContract.createDocument(resolver, parentDocumentUri, "application/pdf", fileName);

            if (newFileUri != null) {
                try (OutputStream outputStream = resolver.openOutputStream(newFileUri)) {
                    pdfDocument.writeTo(outputStream);
                    Toast.makeText(this, "Đã lưu PDF thành công:\n" + fileName, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Không thể tạo file trong thư mục đã chọn.", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) { // Bắt Exception chung để an toàn hơn
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi lưu PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            pdfDocument.close();
        }
    }
}