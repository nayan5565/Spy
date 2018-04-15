package com.example.nayan.appanalysis2.tools;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.example.nayan.appanalysis2.ownSimInfo.SimNoInfo;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Dev on 12/31/2017.
 */

public class Utils {

    public static final String APP_NAME = "Test App";

    public static final String DB_PASS = "test123";

    private Utils() {

    }


    public static boolean postLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean hasUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow("android:get_usage_stats",
                android.os.Process.myUid(), context.getPackageName());
        boolean granted = mode == AppOpsManager.MODE_ALLOWED;
        return granted;
    }

    public static boolean isInternetOn() {

        try {
            ConnectivityManager con = (ConnectivityManager) MainApplication.context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifi, mobile;
            wifi = con.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            mobile = con.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if (wifi.isConnectedOrConnecting() || mobile.isConnectedOrConnecting()) {
                return true;
            }


        } catch (Exception e) {
            // TODO: handle exception
        }
        return false;
    }

    public static String getToday() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String day = sdf.format(new Date());
        return day;
    }

    public static String getPhoneGmailAcc(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType("com.google");
        return accounts.length > 0 ? accounts[0].name.trim().toLowerCase() : "null";

    }

    // get android device id
    public static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    //check dual sim is ready
    private void isDualSimOrNot() {
        TelephonyManager manager = (TelephonyManager) MainApplication.context.getSystemService(Context.TELEPHONY_SERVICE);
        String telNumber = manager.getLine1Number();
        String getSimSerialNumber = manager.getSimSerialNumber();
        SimNoInfo telephonyInfo = SimNoInfo.getInstance(MainApplication.context);

        String imeiSIM1 = telephonyInfo.getImeiSIM1();
        String imeiSIM2 = telephonyInfo.getImeiSIM2();

        boolean isSIM1Ready = telephonyInfo.isSIM1Ready();
        boolean isSIM2Ready = telephonyInfo.isSIM2Ready();

        boolean isDualSIM = telephonyInfo.isDualSIM();
        Log.e("Dual = ", " IME1 : " + imeiSIM1 + "\n" +
                " IME2 : " + imeiSIM2 + "\n" +
                " IS DUAL SIM : " + isDualSIM + "\n" +
                " IS SIM1 READY : " + isSIM1Ready + "\n" +
                " IS SIM2 READY : " + isSIM2Ready + "\n" +
                " own number : " + telNumber + "\n" +
                " own number : " + getSimSerialNumber + "\n");
    }

    public static void log(String tag, String msg) {
        Log.e(tag, msg);
    }

    public static void  toastShow(String msg){
        Toast.makeText(MainApplication.context, msg, Toast.LENGTH_SHORT).show();
    }

    // save data to sharedPreference
    public static void savePref(String name, String value) {
        SharedPreferences pref = MainApplication.context.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(name, value);
        editor.apply();
    }

    // get data from shared preference
    public static String getPref(String name, String defaultValue) {
        SharedPreferences pref = MainApplication.context.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
        return pref.getString(name, defaultValue);
    }

}
