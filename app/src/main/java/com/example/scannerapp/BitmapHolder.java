package com.example.scannerapp;

import android.graphics.Bitmap;

public class BitmapHolder {
    private static Bitmap bitmap;
    private static Bitmap originalBitmap;

    public static void setBitmap(Bitmap b) {
        bitmap = b;
    }

    public static Bitmap getBitmap() {
        return bitmap;
    }

    public static void setOriginalBitmap(Bitmap c){
        originalBitmap = c;
    }
    public static Bitmap getOriginalBitmap(){
        return originalBitmap;
    }
    public static void clear() {
        bitmap = null;
    }
}
