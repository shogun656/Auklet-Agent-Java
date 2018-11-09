package io.auklet.agent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class DataRetention {

    final static private Long MEGABYTES_TO_BYTES = 1000000L;
    final static private Long SECONDS_TO_MILLISECONDS = 1000L;

    static private boolean freshStart = true;
    static private boolean resetData = false;

    static private Long emissionPeriod = 60000L;
    static private Long storageLimit;
    static private Long cellularDataLimit;
    static private int cellPlanDate;

    static private String usageFile;
    static private Long dataSent;
    static private int hours;

    private DataRetention(){ }

    public static void initDataRetention(JSONObject config) throws JSONException, IOException {
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

    public synchronized static void writeToUsageFile(Long usage) {
        if (usageFile != null) {
            try {
                JSONObject usageJson = new JSONObject();
                usageJson.put("usage", usage);

                FileWriter writer = new FileWriter(usageFile);
                writer.write(usageJson.toString());
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("UsageFile needs to be set"); // TODO: change this to a log statement
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
        timer.schedule (hourlyTask, 0L, 1000*60*60);
    }

    public static int getBufferSize() {
        return (storageLimit == 0) ? 5000 : (int)(storageLimit / 5000); // divide by 5KB to get amount of messages
    }
}
