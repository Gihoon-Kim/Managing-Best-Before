package com.example.bestbefore_hmart;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public Bitmap bitmap;
    private BestBeforeManager listManager;
    public static final String TAG = "TAG";
    ImageView iv_photo;
    ListView bestBeforeList;

    private BestBeforeAdapter adapter;

    // create route variable and require variable
    String mCurrentPhotoPath;
    final static int REQUEST_TAKE_PHOTO = 1;

    LayoutInflater inflater;
    View inflateView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                Log.d(TAG, "Permission Setting Complete");
            } else {

                Log.d(TAG, "Permission Setting Request");
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                                Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        },
                        1
                );
            }
        }

        bestBeforeList = findViewById(R.id.item_list);

        listManager = new BestBeforeManager(this);
        adapter = new BestBeforeAdapter(this, listManager.getItems());
        bestBeforeList.setAdapter(adapter);
    }

    @SuppressLint("InflateParams")
    public void mOnClick(View view) {

        switch (view.getId()) {

            case R.id.addItem:

                inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                assert inflater != null;
                inflateView = inflater.inflate(R.layout.add_dialog, null);

                iv_photo = inflateView.findViewById(R.id.testImageView);
                iv_photo.setOnClickListener(imageViewClickListener);

                new AlertDialog.Builder(this)
                        .setView(inflateView)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Add info of EditText


                                DatePicker datePicker = inflateView.findViewById(R.id.datePicker);
                                Date date = new Date(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());

                                EditText itemNameEditText = inflateView.findViewById(R.id.itemNameEditText);
                                String itemName = itemNameEditText.getText().toString();

                                EditText itemBarcodeEditText = inflateView.findViewById(R.id.itemBarcodeEditText);
                                String itemBarcode = itemBarcodeEditText.getText().toString();

                                if (bitmap != null) {
                                    BestBeforeItem bestBeforeItem = new BestBeforeItem(
                                            itemName,
                                            itemBarcode,
                                            date,
                                            bitmap,
                                            false
                                    );
                                    listManager.addItem(bestBeforeItem);

                                    adapter.updateItems(listManager.getItems());

                                    bitmap = null;
                                } else {
                                    Toast.makeText(MainActivity.this, "Image must be added", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .show();

                break;

            case R.id.deleteItem:

                listManager.deleteItem();

                adapter.updateItems(listManager.getItems());
        }
    }

    View.OnClickListener imageViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {

                case R.id.testImageView:

                    dispatchTakePictureIntent();
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            switch (requestCode) {

                case REQUEST_TAKE_PHOTO:

                    if (resultCode == RESULT_OK) {

                        File file = new File(mCurrentPhotoPath);


                        if (Build.VERSION.SDK_INT >= 29) {

                            ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), Uri.fromFile(file));

                            try {

                                bitmap = ImageDecoder.decodeBitmap(source);

                                if (bitmap != null) {

                                    iv_photo.setImageBitmap(bitmap);
                                }
                            } catch (IOException e) {

                                e.printStackTrace();
                            }
                        } else {

                            try {

                                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(file));

                                if (bitmap != null) {

                                    iv_photo.setImageBitmap(bitmap);
                                }
                            } catch (IOException e) {

                                e.printStackTrace();
                            }
                        }
                    }
                    break;
            }
        } catch (Exception error) {

            error.printStackTrace();
        }
    }

    // show thumbnail after taking picture. The image must be saved file
    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyymmdd").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDri = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDri
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // Camera intent processing part
    private void dispatchTakePictureIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            File photoFile = null;

            try {

                photoFile = createImageFile();
            } catch (IOException ex) {
            }

            if (photoFile != null) {

                Uri photoURI = FileProvider.getUriForFile(
                        inflater.getContext(),
                        "com.example.bestbefore_hmart.fileprovider",
                        photoFile
                );

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private class BestBeforeAdapter extends ArrayAdapter<BestBeforeItem> {

        private Context context;
        private List<BestBeforeItem> items;

        public BestBeforeAdapter(
                @NonNull Context context,
                List<BestBeforeItem> items
        ) {
            super(context, -1, items);

            this.context = context;
            this.items = items;
        }

        public void updateItems(List<BestBeforeItem> items) {

            this.items = items;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            final ItemViewHolder holder;

            if (convertView == null) {

                convertView = LayoutInflater.from(context).inflate(
                        R.layout.item_list_layout,
                        parent,
                        false
                );

                holder = new ItemViewHolder();
                holder.itemNameTextView = convertView.findViewById(R.id.itemNameTextView);
                holder.itemCheckBox = convertView.findViewById(R.id.isItemChecked);
                holder.itemBarcodeTextView = convertView.findViewById(R.id.itemBarcodeTextView);
                holder.itemDateTextView = convertView.findViewById(R.id.itemDateTextView);
                holder.itemImageView = convertView.findViewById(R.id.itemImageView);

                convertView.setTag(holder);
            } else {
                holder = (ItemViewHolder) convertView.getTag();
            }

            holder.itemImageView.setImageBitmap(items.get(position).getItemSource());
            holder.itemNameTextView.setText(items.get(position).getProductName());
            holder.itemBarcodeTextView.setText(items.get(position).getBarcode());
            holder.itemCheckBox.setChecked(items.get(position).isChecked());

            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat transFormat = new SimpleDateFormat("yy-MM-dd");
            String date = transFormat.format(items.get(position).getDate());
            holder.itemDateTextView.setText(date + " (yy-mm-dd)");

            holder.itemCheckBox.setTag(items.get(position));

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    BestBeforeItem item = (BestBeforeItem) holder.itemCheckBox.getTag();
                    item.toggleChecked();
                    listManager.updateItem(item);
                    notifyDataSetChanged();
                }
            });

            holder.itemImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Dialog dialog = new Dialog(MainActivity.this);
                    dialog.setContentView(R.layout.item_info);
                    dialog.setTitle("Item Picture");

                    ImageView imageView = (ImageView) dialog.findViewById(R.id.itemInfoImageView);
                    imageView.setImageBitmap(items.get(position).getItemSource());

                    dialog.show();
                }
            });

//            Date currentDate = Calendar.getInstance().getTime();
//
//            SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd");
//            String stringCurrentDate = format.format(currentDate);
//            try {
//                currentDate = format.parse(stringCurrentDate);
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }

//            for (int i = 0; i < items.size(); i++) {
//
//                Log.d("Item Date", items.get(i).getDate().toString());
//                Log.d("current Date", )
//                if (items.get(i).getDate().compareTo(currentDate) < 0) {
//
//                    for (int j = 0; j < items.size(); j++) {
//
//                        if (holder.itemNameTextView.getText() == items.get(i).getProductName()) {
//
//                            holder.itemNameTextView.setBackgroundColor(Color.BLUE);
//                            break;
//                        }
//
//                    }
//
//                }
//            }

            return convertView;
        }
    }

    public static class ItemViewHolder {

        public ImageView itemImageView;
        public TextView itemNameTextView;
        public TextView itemBarcodeTextView;
        public TextView itemDateTextView;
        public CheckBox itemCheckBox;
    }
}
