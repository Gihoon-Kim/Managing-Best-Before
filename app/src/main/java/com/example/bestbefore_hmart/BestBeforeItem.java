package com.example.bestbefore_hmart;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.Date;

public class BestBeforeItem implements Serializable {

    private String productName;
    private String barcode;
    private Date bestBefore;
    private Bitmap itemSource;
    private boolean isChecked;

    private long id;

    public BestBeforeItem(String name, String barcode, Date bestBefore, Bitmap itemSource, boolean isChecked) {
        this(name, barcode, bestBefore, itemSource, isChecked, -1);
    }

    public BestBeforeItem(String productName, String barcode, Date bestBefore, Bitmap itemSource, boolean isChecked, long id) {

        this.productName = productName;
        this.barcode = barcode;
        this.bestBefore = bestBefore;
        this.itemSource = itemSource;
        this.isChecked = isChecked;
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public String getBarcode() {
        return barcode;
    }

    public Date getDate() {
        return bestBefore;
    }

    public Bitmap getItemSource() {
        return itemSource;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public long getId() {
        return id;
    }

    public void toggleChecked() {
        isChecked = !isChecked;
    }

    // convert from bitmap to byte array
    public static byte[] getBytes(Bitmap bitmap) {

        Bitmap resized = Bitmap.createScaledBitmap(bitmap, 100, 100, true);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}
