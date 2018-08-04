package com.lukasz.smsr;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Lukasz on 2018-06-03.
 */

public class DBHelper extends SQLiteOpenHelper {

    public static final int VERSION = 4;
    public static final String DATABASE_NAME="Messages";
    public static final String TABLE_MESSAGES ="messages";
    public static final String TABLE_IMEI = "imei";

    public static final String COLUMN_ID="id";
    public static final String COLUMN_DATE="date";
    public static final String COLUMN_SENDER="sender";
    public static final String COLUMN_MESSAGE="message";
    public static final String COLUMN_IMEI = "imei";

    private String sqlCreateMessagesTable = "CREATE TABLE "+ TABLE_MESSAGES +" ("+COLUMN_ID+" INTEGER PRIMARY KEY,"+COLUMN_DATE+" LONG,"+COLUMN_SENDER+" TEXT,"+COLUMN_MESSAGE+" TEXT)";
    private String sqlCreateIMEITable = "CREATE TABLE " + TABLE_IMEI+" ("+COLUMN_IMEI+" TEXT)";


    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        System.out.println(sqlCreateMessagesTable);
        System.out.println(sqlCreateIMEITable);
        sqLiteDatabase.execSQL(sqlCreateMessagesTable);
        sqLiteDatabase.execSQL(sqlCreateIMEITable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_MESSAGES);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_IMEI);
        onCreate(sqLiteDatabase);
    }
}
