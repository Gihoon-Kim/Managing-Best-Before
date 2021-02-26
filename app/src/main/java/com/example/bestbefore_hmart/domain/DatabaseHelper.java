package com.example.bestbefore_hmart.domain;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "items.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_NAME = "items";
    public static final String ID = "_id";
    public static final String PRODUCT_NAME = "productName";
    public static final String BARCODE = "barcode";
    public static final String CHECKED = "checked";
    public static final String BEST_BEFORE = "bestBefore";
    public static final String IMAGE = "image";

    private static DatabaseHelper instance = null;

    public static DatabaseHelper getInstance(Context context) {

        if (instance == null) {

            instance = new DatabaseHelper(context);
        }

        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        /*
        String createQuery = "CREATE TABLE " + TABLE_NAME +
                " (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " productName TEXT NOT NULL, " +
                " barcode TEXT NOT NULL, " +
                " bestbefore TEXT NOT NULL, " +
                " checked INTEGER NOT NULL DEFAULT 0," +
                " image BLOB)";
         */

        String createQuery = "CREATE TABLE " + TABLE_NAME +
                " (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " productName TEXT NOT NULL," +
                " barcode TEXT NOT NULL, " +
                " bestBefore TEXT NOT NULL, " +
                " checked INTEGER NOT NULL DEFAULT 0," +
                " image BLOB NOT NULL)";

        db.execSQL(createQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
