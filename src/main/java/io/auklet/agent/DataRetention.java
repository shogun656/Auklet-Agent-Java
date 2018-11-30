package io.auklet.agent;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public final class DataRetention {


    private static Logger logger = LoggerFactory.getLogger(DataRetention.class);

    private static final Long MEGABYTES_TO_BYTES = 1000000L;
    private static final Long SECONDS_TO_MILLISECONDS = 1000L;

    private static boolean freshStart = true;
    private static boolean resetData = false;

    private static Long emissionPeriod = 60000L;
    private static Long storageLimit;
    private static Long cellularDataLimit;
    private static int cellPlanDate;

    private static String usageFile;
    private static final Object usageFileLock = new Object();
    private static Long dataSent;
    private static int hours;

    private DataRetention(){ }

    public static void initDataRetention(JSONObject config) throws IOException {
        emissionPeriod = config.getLong("emission_period") * SECONDS_TO_MILLISECONDS;
        storageLimit = config.getJSONObject("storage").optLong("storage_limit") * MEGABYTES_TO_BYTES;
        cellularDataLimit = config.getJSONObject("data").optLong("cellular_data_limit") * MEGABYTES_TO_BYTES;
        cellPlanDate = config.getJSONObject("data").getInt("normalized_cell_plan_date");

        dataSent = new JSONObject(new String(Files.readAllBytes(Paths.get(usageFile)))).getLong("usage");

        if (freshStart) {
            checkDate();
            freshStart = false;
        }
    }

    public static void updateDataSent(int dataSize) {
        dataSent += dataSize;
        writeToUsageFile(dataSent);
    }

    public static Boolean hasNotExceededDataLimit(int dataSize) {
        return ((cellularDataLimit == 0) || (dataSent + dataSize <= cellularDataLimit));
    }

    public static void setUsageFile(String file) throws IOException {
        usageFile = file;
        File usage = new File(file);
        if (usage.createNewFile()) {
            writeToUsageFile(0L);
        }
    }

    public static void writeToUsageFile(Long usage) {
        synchronized (usageFileLock) {
            if (usageFile != null) {
                try (FileWriter writer = new FileWriter(usageFile)) {
                    JSONObject usageJson = new JSONObject();
                    usageJson.put("usage", usage);
                    writer.write(usageJson.toString());
                } catch (IOException e) {
                    logger.error("Unable to access Usage File", e);
                }
            } else {
                logger.error("UsageFile needs to be set");
            }
        }
    }

    private static void checkDate() {
        Timer timer = new Timer();
        TimerTask hourlyTask = new TimerTask() {
            @Override
            public void run () {
                hours++;
                if (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == cellPlanDate) {
                    if (resetData) {
                        dataSent = 0L;
                        resetData = false;
                    }
                } else {
                    resetData = true;
                }

                if (hours >= 24) {
                    Device.initLimitsConfig(); // Refresh the config once a day
                }
            }
        };

        // schedule the task to run starting now and then every hour...
        timer.schedule (hourlyTask, 0L, 1000L*60*60);
    }

    public static int getBufferSize() {
        return (storageLimit == 0) ? 5000 : (int)(storageLimit / 5000); // divide by 5KB to get amount of messages
    }
}
