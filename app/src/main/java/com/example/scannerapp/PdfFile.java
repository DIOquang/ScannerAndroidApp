package com.example.scannerapp; // Thay bằng package của bạn

import android.net.Uri;

// Lớp này dùng để chứa thông tin của một file PDF
public class PdfFile {
    final Uri uri;
    final String name;
    final long dateModified;
    final long size;

    public PdfFile(Uri uri, String name, long dateModified, long size) {
        this.uri = uri;
        this.name = name;
        this.dateModified = dateModified;
        this.size = size;
    }

    public Uri getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public long getDateModified() {
        return dateModified;
    }

    public long getSize() {
        return size;
    }
}