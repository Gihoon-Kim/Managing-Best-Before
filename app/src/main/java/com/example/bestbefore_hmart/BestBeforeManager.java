package com.example.bestbefore_hmart;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ListView;

import com.example.bestbefore_hmart.domain.DatabaseHelper;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BestBeforeManager {

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private byte[] imageByte;

    BestBeforeManager(Context context) {

        dbHelper = DatabaseHelper.getInstance(context);
    }

    List<BestBeforeItem> getItems() {

        db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                " SELECT * FROM " + DatabaseHelper.TABLE_NAME +
                        " ORDER BY " + DatabaseHelper.BEST_BEFORE,
                null
        );

        List<BestBeforeItem> items = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {

            while (!cursor.isAfterLast()) {

                // common format to transform from string (SQLite) to Date (Constructor class)
                String error = "";

                // to change date format from database as string format to constructor class as Date format
                DateFormat transFormat = new SimpleDateFormat("yy-MM-dd");
                Date date = null;
                try {
                    date = transFormat.parse(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.BEST_BEFORE)));
                } catch (ParseException e) {
                    Log.e(error, "Date is null");
                }

                // get Bitmap from byte Array
                byte[] imageByte = cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.IMAGE));
                Bitmap bitmap = BestBeforeItem.getImage(imageByte);


                BestBeforeItem item = new BestBeforeItem(
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.PRODUCT_NAME)),
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.BARCODE)),
                        date,
                        bitmap,
                        cursor.getInt(cursor.getColumnIndex(DatabaseHelper.CHECKED)) != 0,
                        cursor.getLong(cursor.getColumnIndex(DatabaseHelper.ID))
                );
                items.add(item);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return items;
    }

    void addItem(BestBeforeItem item) {

        SimpleDateFormat fm = new SimpleDateFormat("yy-MM-dd");
        String to = fm.format(item.getDate());

        imageByte = BestBeforeItem.getBytes(item.getItemSource());

        ContentValues newItem = new ContentValues();
        newItem.put(DatabaseHelper.PRODUCT_NAME, item.getProductName());
        newItem.put(DatabaseHelper.BARCODE, item.getBarcode());
        newItem.put(DatabaseHelper.BEST_BEFORE, to);
        newItem.put(DatabaseHelper.IMAGE, imageByte);
        newItem.put(DatabaseHelper.CHECKED, item.isChecked());

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.insert(DatabaseHelper.TABLE_NAME, null, newItem);
    }

    public void updateItem(BestBeforeItem item) {

        // UPDATE items
        // SET productName = "", completed = ""
        // WHERE _id = ""

        SimpleDateFormat fm = new SimpleDateFormat("yy-MM-dd");
        String to = fm.format(item.getDate());

        ContentValues updateItem = new ContentValues();
        updateItem.put(DatabaseHelper.PRODUCT_NAME, item.getProductName());
        updateItem.put(DatabaseHelper.BARCODE, item.getBarcode());
        updateItem.put(DatabaseHelper.BEST_BEFORE, to);
        updateItem.put(DatabaseHelper.CHECKED, item.isChecked());
        updateItem.put(DatabaseHelper.IMAGE, BestBeforeItem.getBytes(item.getItemSource()));

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String[] args = new String[] {
                String.valueOf(item.getId())
        };

        db.update(
                DatabaseHelper.TABLE_NAME,
                updateItem,
                DatabaseHelper.ID + "=?",
                args
        );
    }

    public void deleteItem() {

        try {
            List<BestBeforeItem> items = getItems();

            for (int i = items.size() - 1; i >= 0; i--) {

                if (items.get(i).isChecked()) {

                    items.remove(items.get(i));
                    db.execSQL(
                            "DELETE FROM " + DatabaseHelper.TABLE_NAME +
                                    " WHERE " + DatabaseHelper.CHECKED + " = 1"
                    );
                }
            }
        } catch (Exception e) {
            Log.d("Delete Error", e.getMessage());
        }
    }
}
