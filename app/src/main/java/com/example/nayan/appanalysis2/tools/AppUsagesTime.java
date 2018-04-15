package com.example.nayan.appanalysis2.tools;

import android.annotation.TargetApi;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.os.Build;
import android.util.Log;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by Dev on 1/8/2018.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class AppUsagesTime {
    UsageStats usageStats;

    String PackageName = "Nothing";

    long TimeInforground = 500;

    int minutes = 500, seconds = 500, hours = 500;
    UsageStatsManager mUsageStatsManager = (UsageStatsManager) MainApplication.context.getSystemService(Service.USAGE_STATS_SERVICE);

    long time = System.currentTimeMillis();

    public List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time);

    public void usageTime() {
        if (stats != null) {
            SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
            for (UsageStats usageStats2 : stats) {

                TimeInforground = usageStats2.getTotalTimeInForeground();

                PackageName = usageStats2.getPackageName();

                minutes = (int) ((TimeInforground / (1000 * 60)) % 60);

                seconds = (int) (TimeInforground / 1000) % 60;

                hours = (int) ((TimeInforground / (1000 * 60 * 60)) % 24);

                Log.e("BAC", "PackageName is" + PackageName + "Time is: " + hours + "h" + ":" + minutes + "m" + seconds + "s");

            }
        }
    }

}
