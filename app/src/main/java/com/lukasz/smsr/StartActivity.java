package com.lukasz.smsr;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.widget.ImageView;

/**
 * Created by Lukasz on 2018-05-22.
 */

public class StartActivity extends android.app.Activity implements ActivityCompat.OnRequestPermissionsResultCallback {

    ImageView smsImage, phoneImage, statusImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        smsImage = findViewById(R.id.smsImage);
        phoneImage = findViewById(R.id.phoneImage);
        statusImage = findViewById(R.id.statusImage);
        checkPermissions();
    }


    private void checkPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            smsImage.setImageResource(R.drawable.x);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, 123);
        }
        else smsImage.setImageResource(R.drawable.ok);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            phoneImage.setImageResource(R.drawable.x);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 124);
        }
        else phoneImage.setImageResource(R.drawable.ok);

        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor imeiCursor = db.query(DBHelper.TABLE_IMEI, null, null, null, null, null, null, null);
        if(imeiCursor.getCount()>0) statusImage.setImageResource(R.drawable.ok);
        else {
            statusImage.setImageResource(R.drawable.x);
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
                TelephonyManager telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
                String imei = telephonyManager.getDeviceId();
                SQLiteDatabase saveDb = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(DBHelper.COLUMN_IMEI, imei);
                saveDb.insert(DBHelper.TABLE_IMEI, null, values);
                checkPermissions();
            }
        }
    }


    //zmienia ikonki po odpowiedzi na zapytanie o uprawnienia
    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults){
        if(requestCode == 123){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) smsImage.setImageResource(R.drawable.x);
            else smsImage.setImageResource(R.drawable.ok);
        }
        else if(requestCode == 124){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)phoneImage.setImageResource(R.drawable.x);
            else phoneImage.setImageResource(R.drawable.ok);
        }
    }
}
