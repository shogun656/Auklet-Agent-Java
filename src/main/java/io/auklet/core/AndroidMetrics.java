package io.auklet.core;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public final class AndroidMetrics {

    private static final Logger LOGGER = LoggerFactory.getLogger(AndroidMetrics.class);
    private ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
    private ActivityManager manager;
    private ScheduledThreadPoolExecutor cpuThread;

    private static Long total, totalBefore, totalDiff, work, workBefore, workDiff;
    private static float cpuUsage = 0;

    public AndroidMetrics(Context context) {
        manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        manager.getMemoryInfo(memInfo);
        total = totalBefore = totalDiff = work = workBefore = workDiff = 0L;

        // On Android 8+, Google has restricted access to the proc files
        if (Build.VERSION.SDK_INT < 26) {
            cpuThread =  new ScheduledThreadPoolExecutor(1, Util.createDaemonThreadFactory("Android-Metrics"));
            cpuThread.scheduleAtFixedRate(this.calculateCPUUsage(), 0L, 1L, TimeUnit.SECONDS);
        } else {
            LOGGER.debug("We are unable to attain CPU data on Devices running Android 8+");
        }
    }

    public Runnable calculateCPUUsage() {
        return new Runnable() {
            @Override
            public void run() {
                try (BufferedReader reader = new BufferedReader(new FileReader("/proc/stat"))) {
                    // Obtain current CPU Load
                    String[] s = reader.readLine().split("[ ]+", 9);
                    work = Long.parseLong(s[1]) + Long.parseLong(s[2]) + Long.parseLong(s[3]);
                    total = work + Long.parseLong(s[4]) + Long.parseLong(s[5]) +
                            Long.parseLong(s[6]) + Long.parseLong(s[7]);
                } catch (IOException e) {
                    Log.e("auklet", "Unable to obtain CPU Usage", e);
                    return;
                }

                // Calculate CPU Percentage
                if (totalBefore != 0) {
                    workDiff = work - workBefore;
                    totalDiff = total - totalBefore;
                    cpuUsage = restrictPercentage(workDiff * 100 / (float) totalDiff);
                }

                totalBefore = total;
                workBefore = work;
            }
        };
    }

    /** <p>Shuts down the daemon threads that refresh the config and track data usage.</p> */
    public void shutdown() {
        if (cpuThread != null) {
            this.cpuThread.shutdown();
        }
    }

    public double getMemorUsagey() {
        // memInfo.totalMem needs API 16+
        return memInfo.availMem / (double) memInfo.totalMem * 100.0;
    }

    public float getCPUUsage() {
        return cpuUsage;
    }

    private float restrictPercentage(float percentage) {
        if (percentage > 100)
            return 100;
        else if (percentage < 0)
            return 0;
        else return percentage;
    }
}
