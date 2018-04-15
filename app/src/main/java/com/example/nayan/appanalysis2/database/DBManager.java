package com.example.nayan.appanalysis2.database;

import android.content.ContentValues;
import android.util.Log;

import com.example.nayan.appanalysis2.model.MCalllog;
import com.example.nayan.appanalysis2.model.MContact;
import com.example.nayan.appanalysis2.model.MScreenshot;
import com.example.nayan.appanalysis2.model.MSms;
import com.example.nayan.appanalysis2.tools.MainApplication;
import com.example.nayan.appanalysis2.tools.Utils;
import com.google.gson.Gson;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Dev on 12/27/2017.
 */
public class DBManager {
    public static final String TABLE_CALL_LOG = "tbl_call_log";
    public static final String TABLE_SCREENSHOT = "tbl_screenshot";
    public static final String TABLE_CONTACTS = "tbl_contacts";
    public static final String TABLE_SMS = "tbl_sms";

    private static final String DB_NAME = "test_app.db";


    private static final String CREATE_TABLE_SCREENSHOT = DBQuery.init()
            .newTable(TABLE_SCREENSHOT)
            .addField("id", DBQuery.INTEGER_PRI_AUTO)
            .addField("imgName", DBQuery.TEXT)
            .getTable();

    private static final String CREATE_TABLE_CALL_LOG = DBQuery.init()
            .newTable(TABLE_CALL_LOG)
            .addField("id", DBQuery.INTEGER_PRI)
            .addField("number", DBQuery.TEXT)
            .addField("duration", DBQuery.TEXT)
            .addField("callDate", DBQuery.TEXT)
            .addField("type", DBQuery.TEXT)
            .getTable();
    private static final String CREATE_TABLE_CONTACTS = DBQuery.init()
            .newTable(TABLE_CONTACTS)
            .addField("id", DBQuery.INTEGER_PRI)
            .addField("displayName", DBQuery.TEXT)
            .addField("normilizedPhone", DBQuery.TEXT)
            .addField("phone", DBQuery.TEXT)
            .getTable();
    private static final String CREATE_TABLE_SMS = DBQuery.init()
            .newTable(TABLE_SMS)
            .addField("id", DBQuery.INTEGER_PRI)
            .addField("address", DBQuery.TEXT)
            .addField("body", DBQuery.TEXT)
            .addField("type", DBQuery.TEXT)
            .addField("sentDate", DBQuery.TEXT)
            .addField("receivedDate", DBQuery.TEXT)
            .getTable();


    private static DBManager instance;
    private final String TAG = getClass().getSimpleName();
    private SQLiteDatabase db;

    private DBManager() {
        openDB();
        createTable();
    }

    public static DBManager getInstance() {
        if (instance == null)
            instance = new DBManager();
        return instance;
    }

    public static String getQueryDate(String table, String primaryKey) {
        return "select * from " + table + " where " + primaryKey + "='";
    }

    public static String getQueryAll(String table) {
        return "select * from " + table;
    }

    private void openDB() {
        SQLiteDatabase.loadLibs(MainApplication.context);
        File databaseFile = MainApplication.context.getDatabasePath(DB_NAME);
        if (!databaseFile.exists()) {
            databaseFile.mkdirs();
            databaseFile.delete();
        }
        db = SQLiteDatabase.openOrCreateDatabase(databaseFile, Utils.DB_PASS, null);
    }

    private void createTable() {
        db.execSQL(CREATE_TABLE_SCREENSHOT);
        db.execSQL(CREATE_TABLE_CALL_LOG);
        db.execSQL(CREATE_TABLE_CONTACTS);
        db.execSQL(CREATE_TABLE_SMS);
    }

    private boolean isExist(String table, String searchField, String value) {
        if (value.equals("") || Integer.valueOf(value) <= 0)
            return false;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + table + " where " + searchField + "='" + value + "'", null);
            if (cursor != null && cursor.getCount() > 0)
                return true;
        } catch (Exception e) {

        } finally {
            if (cursor != null)
                cursor.close();

        }


        return false;
    }


    public void deleteData(String table, String primaryKey, String value) {
        if (!isExist(table, primaryKey, value))
            return;

        int r = db.delete(table, primaryKey + "=?", new String[]{value});
        Log.e("db", "deleted " + r);
    }

    public void close() {
        try {
            if (db.isOpen())
                db.close();
        } catch (Exception e) {
        }

    }

    public long addScreenshot(MScreenshot mScreenshot) {
        long id = 0;
        android.database.Cursor cursor = null;
        try {
            ContentValues values = new ContentValues();
            values.put("imgName", mScreenshot.getImgName());


            String sql = "select * from " + TABLE_SCREENSHOT + " where imgName='" + mScreenshot.getImgName() + "'";
            cursor = db.rawQuery(sql, null);
            Log.e("cu", "has" + cursor);
            if (cursor != null && cursor.getCount() > 0) {
                int update = db.update(TABLE_SCREENSHOT, values, "imgName=?", new String[]{mScreenshot.getImgName() + ""});
                Log.e("Image", "image update : " + update);
            } else {
                long v = db.insert(TABLE_SCREENSHOT, null, values);
                id = v;
                Log.e("Image", "image insert : " + v);

            }


        } catch (Exception e) {

        }
        if (cursor != null)
            cursor.close();
        return id;
    }


    public ArrayList<MScreenshot> getScreenshot() {
        ArrayList<MScreenshot> assetArrayList = new ArrayList<>();
        Log.e("DB", "S1");
        Gson gson = new Gson();
        MScreenshot mScreenshot;
        String sql = "select * from " + TABLE_SCREENSHOT;
        android.database.Cursor cursor = db.rawQuery(sql, null);
        if (cursor != null && cursor.moveToFirst()) {
            Log.e("DB", "S2 :" + cursor.getCount());
            do {
                mScreenshot = new MScreenshot();
                mScreenshot.setId(cursor.getInt(cursor.getColumnIndex("id")));
                mScreenshot.setImgName(cursor.getString(cursor.getColumnIndex("imgName")));


                assetArrayList.add(mScreenshot);

            } while (cursor.moveToNext());

        }
        cursor.close();


        return assetArrayList;
    }


    public void addCallLog(MCalllog mCalllog, String tableName) {

        android.database.Cursor cursor = null;
        try {
            ContentValues values = new ContentValues();
            values.put("id", mCalllog.getId());
            values.put("type", mCalllog.getType());
            values.put("number", mCalllog.getNumber());
            values.put("duration", mCalllog.getDuration());
            values.put("callDate", mCalllog.getCallDate());
            String sql = "select * from " + tableName + " where id='" + mCalllog.getId() + "'";
            cursor = db.rawQuery(sql, null);
            Log.e("cu", "has" + cursor);
            if (cursor != null && cursor.getCount() > 0) {
                int update = db.update(tableName, values, "id=?", new String[]{mCalllog.getId() + ""});
                Log.e("calllog", " update : " + update);
            } else {
                long v = db.insert(tableName, null, values);
                Log.e("calllog", " insert : " + v);

            }


        } catch (Exception e) {

        }
        if (cursor != null)
            cursor.close();
    }

    public void addContacts(MContact mContact, String tableName) {

        android.database.Cursor cursor = null;
        try {
            ContentValues values = new ContentValues();
            values.put("id", mContact.getId());
            values.put("displayName", mContact.getDisplayName());
            values.put("normilizedPhone", mContact.getNormilizedPhone());
            values.put("phone", mContact.getPhone());
            String sql = "select * from " + tableName + " where id='" + mContact.getId() + "'";
            cursor = db.rawQuery(sql, null);
            Log.e("contacts", "cursor has " + cursor);
            if (cursor != null && cursor.getCount() > 0) {
                int update = db.update(tableName, values, "id=?", new String[]{mContact.getId() + ""});
                Log.e("contacts", " update : " + update);
            } else {
                long v = db.insert(tableName, null, values);
                Log.e("contacts", " insert : " + v);

            }


        } catch (Exception e) {

        }
        if (cursor != null)
            cursor.close();
    }

    public void addSms(MSms mSms, String tableName) {

        android.database.Cursor cursor = null;
        try {
            ContentValues values = new ContentValues();
            values.put("id", mSms.getId());
            values.put("type", mSms.getType());
            values.put("address", mSms.getAddress());
            values.put("body", mSms.getBody());
            values.put("sentDate", mSms.getSentDate());
            values.put("receivedDate", mSms.getReceivedDate());
            String sql = "select * from " + tableName + " where id='" + mSms.getId() + "'";
            cursor = db.rawQuery(sql, null);
            Log.e("sms", "cursor has" + cursor);
            if (cursor != null && cursor.getCount() > 0) {
                int update = db.update(tableName, values, "id=?", new String[]{mSms.getId() + ""});
                Log.e("sms", " update : " + update);
            } else {
                long v = db.insert(tableName, null, values);
                Log.e("sms", " insert : " + v);

            }


        } catch (Exception e) {

        }
        if (cursor != null)
            cursor.close();
    }


    public ArrayList<MCalllog> getCallLog() {
        ArrayList<MCalllog> assetArrayList = new ArrayList<>();
        Log.e("DB", "S1");
        Gson gson = new Gson();
        MCalllog mCalllog;
        String sql = "select * from " + TABLE_CALL_LOG;
        android.database.Cursor cursor = db.rawQuery(sql, null);
        if (cursor != null && cursor.moveToFirst()) {
            Log.e("DB", "S2 :" + cursor.getCount());
            do {
                mCalllog = new MCalllog();
                mCalllog.setId(cursor.getInt(cursor.getColumnIndex("id")));
                mCalllog.setNumber(cursor.getString(cursor.getColumnIndex("number")));
                mCalllog.setCallDate(cursor.getString(cursor.getColumnIndex("callDate")));
                mCalllog.setDuration(cursor.getString(cursor.getColumnIndex("duration")));
                mCalllog.setType(cursor.getString(cursor.getColumnIndex("type")));


                assetArrayList.add(mCalllog);

            } while (cursor.moveToNext());

        }
        cursor.close();


        return assetArrayList;
    }

    public ArrayList<MSms> getSms() {
        ArrayList<MSms> assetArrayList = new ArrayList<>();
        Log.e("DB", "S1");
        Gson gson = new Gson();
        MSms mSms;
        String sql = "select * from " + TABLE_SMS;
        android.database.Cursor cursor = db.rawQuery(sql, null);
        if (cursor != null && cursor.moveToFirst()) {
            Log.e("DB", "S2 :" + cursor.getCount());
            do {
                mSms = new MSms();
                mSms.setId(cursor.getInt(cursor.getColumnIndex("id")));
                mSms.setAddress(cursor.getString(cursor.getColumnIndex("address")));
                mSms.setBody(cursor.getString(cursor.getColumnIndex("body")));
                mSms.setSentDate(cursor.getString(cursor.getColumnIndex("sentDate")));
                mSms.setType(cursor.getString(cursor.getColumnIndex("type")));
                mSms.setReceivedDate(cursor.getString(cursor.getColumnIndex("receivedDate")));

                assetArrayList.add(mSms);

            } while (cursor.moveToNext());

        }
        cursor.close();


        return assetArrayList;
    }

    public ArrayList<MContact> getContacts() {
        ArrayList<MContact> assetArrayList = new ArrayList<>();
        Log.e("DB", "S1");
        Gson gson = new Gson();
        MContact mContact;
        String sql = "select * from " + TABLE_CONTACTS;
        android.database.Cursor cursor = db.rawQuery(sql, null);
        if (cursor != null && cursor.moveToFirst()) {
            Log.e("DB", "S2 :" + cursor.getCount());
            do {
                mContact = new MContact();
                mContact.setId(cursor.getInt(cursor.getColumnIndex("id")));
                mContact.setDisplayName(cursor.getString(cursor.getColumnIndex("displayName")));
                mContact.setNormilizedPhone(cursor.getString(cursor.getColumnIndex("normilizedPhone")));
                mContact.setPhone(cursor.getString(cursor.getColumnIndex("phone")));


                assetArrayList.add(mContact);

            } while (cursor.moveToNext());

        }
        cursor.close();


        return assetArrayList;
    }


}
