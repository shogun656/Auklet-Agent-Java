package io.auklet.core;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <p>This class handles retrieving the Memory and CPU Usage for Android devices.</p>
 *
 * <p>CPU Usage can only be retrieved for devices running on Android 7 or older. If your device is running on
 * something newer than Android 7 than the CPU Usage will return 0.</p>
 */
public final class AndroidMetrics {

    private static final Logger LOGGER = LoggerFactory.getLogger(AndroidMetrics.class);
    private ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
    private ActivityManager manager;
    private ScheduledThreadPoolExecutor cpuThread;

    private Long total;
    private Long totalBefore;
    private Long totalDiff;
    private Long work;
    private Long workBefore;
    private Long workDiff;
    private float cpuUsage = 0;

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

    private Runnable calculateCPUUsage() {
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
                    LOGGER.error("Unable to obtain CPU Usage", e);
                    return;
                }

                // Calculate CPU Percentage
                if (totalBefore != 0) {
                    workDiff = work - workBefore;
                    totalDiff = total - totalBefore;
                    cpuUsage = workDiff * 100 / (float) totalDiff;
                }

                totalBefore = total;
                workBefore = work;
            }
        };
    }

    /** <p>Shuts down the daemon threads that calculates the CPU Usage.</p> */
    public void shutdown() {
        if (cpuThread != null) {
            this.cpuThread.shutdown();
        }
    }

    /** <p>Returns the Memory Usage for this Android device.</p> */
    @NonNull public double getMemoryUsage() {
        // memInfo.totalMem needs API 16+
        return memInfo.availMem / (double) memInfo.totalMem * 100.0;
    }

    /** <p>Returns the CPU Usage for this Android device. Return 0 if Android 8+</p> */
    @NonNull public float getCPUUsage() {
        return cpuUsage;
    }
}
