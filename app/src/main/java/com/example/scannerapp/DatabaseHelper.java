package com.example.scannerapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Tên database và phiên bản
    private static final String DATABASE_NAME = "UserManager.db";
    private static final int DATABASE_VERSION = 1;

    // Tên bảng và các cột
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USER_EMAIL = "user_email";
    private static final String COLUMN_USER_PASSWORD = "user_password";

    // Câu lệnh tạo bảng users
    private static final String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USER_EMAIL + " TEXT UNIQUE,"
            + COLUMN_USER_PASSWORD + " TEXT" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Thực thi câu lệnh tạo bảng
        db.execSQL(CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xóa bảng cũ nếu nó tồn tại
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        // Tạo lại bảng
        onCreate(db);
    }

    /**
     * Phương thức này để thêm người dùng mới vào database
     */
    public boolean addUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_EMAIL, email);
        values.put(COLUMN_USER_PASSWORD, password);

        // Chèn một hàng mới, db.insert sẽ trả về -1 nếu có lỗi
        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1; // Trả về true nếu chèn thành công, false nếu thất bại
    }

    /**
     * Phương thức này để kiểm tra xem người dùng có tồn tại trong database không
     */
    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Các cột cần lấy
        String[] columns = {COLUMN_USER_ID};
        // Mệnh đề WHERE
        String selection = COLUMN_USER_EMAIL + " = ?" + " AND " + COLUMN_USER_PASSWORD + " = ?";
        // Các giá trị cho mệnh đề WHERE
        String[] selectionArgs = {email, password};

        // Thực hiện truy vấn
        Cursor cursor = db.query(TABLE_USERS, // Tên bảng
                columns,                    // Các cột trả về
                selection,                  // Mệnh đề WHERE
                selectionArgs,              // Giá trị cho WHERE
                null,                       // group by
                null,                       // having
                null);                      // order by

        int cursorCount = cursor.getCount();
        cursor.close();
        db.close();

        return cursorCount > 0; // Trả về true nếu tìm thấy người dùng, false nếu không
    }
}