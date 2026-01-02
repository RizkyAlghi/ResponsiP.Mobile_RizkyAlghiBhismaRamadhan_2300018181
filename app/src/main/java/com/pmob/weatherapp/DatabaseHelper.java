package com.pmob.weatherapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// Tambahkan import ini agar List dan ArrayList bisa digunakan
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Nama Database & Versi
    private static final String DATABASE_NAME = "WeatherDB";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL untuk membuat tabel (Syarat Database CRUD)
        String CREATE_TABLE = "CREATE TABLE history (id INTEGER PRIMARY KEY AUTOINCREMENT, city_name TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS history");
        onCreate(db);
    }

    // --- FITUR CRUD ---

    // 1. CREATE (Tambah data)
    public void addHistory(String cityName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("city_name", cityName);
        db.insert("history", null, values);
        db.close();
    }

    // 2. READ (Ambil data terakhir untuk ditampilkan saat aplikasi dibuka)
    public String getLastCity() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM history ORDER BY id DESC LIMIT 1", null);
        String city = "Jakarta"; // default jika database kosong
        if (cursor.moveToFirst()) {
            city = cursor.getString(1);
        }
        cursor.close();
        return city;
    }

    // 3. READ ALL (Fungsi tambahan untuk mengecek total data di Database)
    // Ini yang akan menghilangkan error "merah" di MainActivity
    public List<String> getAllHistory() {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM history", null);

        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(1)); // Ambil kolom city_name
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // 4. DELETE (Hapus data - melengkapi syarat CRUD)
    public void deleteAllHistory() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM history");
        db.close();
    }
}
