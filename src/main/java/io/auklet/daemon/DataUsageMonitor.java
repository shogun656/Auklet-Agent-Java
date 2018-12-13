package io.auklet.daemon;

import io.auklet.config.DataUsageLimit;
import io.auklet.config.DataUsageTracker;

import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <p>This class handles tracking of data usage, enforcement of data usage limits, and periodic refresh
 * of data usage limit config.</p>
 */
public class DataUsageMonitor {

    private final Object lock = new Object();
    private final DataUsageLimit limit;
    private final DataUsageTracker tracker;
    private final ScheduledThreadPoolExecutor threadPool;
    private boolean awaitingMonthlyReset = false;
    private int hoursSinceConfigRefresh = 0;

    /**
     * <p>Constructor.</p>
     *
     * @param limit the data usage limit config, never {@code null}.
     * @param tracker the data usage tracker object, never {@code null}.
     */
    public DataUsageMonitor(DataUsageLimit limit, DataUsageTracker tracker) {
        this.limit = limit;
        this.tracker = tracker;
        this.threadPool = new ScheduledThreadPoolExecutor(2, (Runnable r) -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        });
        this.threadPool.scheduleAtFixedRate(this.createDataResetTask(), 0L, 1L, TimeUnit.DAYS);
        this.threadPool.scheduleAtFixedRate(this.createLimitConfigRefreshTask(), 0L, 1L, TimeUnit.HOURS);
    }

    /**
     * <p>Adds the input number of bytes to the current amount of bytes sent.</p>
     *
     * @param moreBytes no-op if less than 1.
     */
    public void addMoreData(int moreBytes) {
        if (moreBytes < 1) return;
        synchronized (this.lock) {
            this.tracker.addMoreData(moreBytes);
        }
    }

    /**
     * <p>Determines whether or not the given proposed payload size would exceed the data limit.</p>
     *
     * @param proposedPayloadSize the size of the payload in bytes.
     * @return {@code true} if sending this payload to the sink would exceed the data limit, {@code false}
     * otherwise.
     */
    public boolean willExceedLimit(long proposedPayloadSize) {
        if (proposedPayloadSize <= 0) return true;
        synchronized (this.lock) {
            long dataLimit = this.limit.getCellularDataLimit();
            return (dataLimit == 0) || (this.tracker.getBytesSent() + proposedPayloadSize <= dataLimit);
        }
    }

    /** <p>Shuts down the daemon threads that refresh the config and track data usage.</p> */
    public void shutdown() {
        this.threadPool.shutdown();
    }

    /**
     * <p>Creates the periodic task that resets data usage once a month.</p>
     *
     * @return never {@code null}.
     */
    private Runnable createDataResetTask() {
        return () -> {
            synchronized (this.lock) {
                if (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == this.limit.getCellPlanDate()) {
                    if (awaitingMonthlyReset) {
                        this.tracker.reset();
                        awaitingMonthlyReset = false;
                    }
                } else {
                    awaitingMonthlyReset = true;
                }
            }
        };
    }

    /**
     * <p>Creates the periodic task that refreshes the limit config from the API once a day.</p>
     *
     * @return never {@code null}.
     */
    private Runnable createLimitConfigRefreshTask() {
        return () -> {
            synchronized (this.lock) {
                hoursSinceConfigRefresh++;
                if (hoursSinceConfigRefresh == 24) {
                    this.limit.refresh();
                    hoursSinceConfigRefresh = 0;
                }
            }
        };
    }

}
