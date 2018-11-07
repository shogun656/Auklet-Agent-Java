package io.auklet.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DataRetention {

    final static private Long MEGABYTES_TO_BYTES = 1000000L;
    final static private Long SECONDS_TO_MILLISECONDS = 1000L;

    static private Long emissionPeriod = 60000L;
    static private Long storageLimit;
    static private Long cellularDataLimit;
    static private int cellPlanDate;
    static private Long dataSent;
    static private String usageFile;

    private DataRetention(){ }

    public static void initDataRetention(JSONObject config) throws JSONException, IOException {
        emissionPeriod = config.getLong("emission_period") * SECONDS_TO_MILLISECONDS;
        storageLimit = config.getJSONObject("storage").getLong("storage_limit") * MEGABYTES_TO_BYTES;
        cellularDataLimit = config.getJSONObject("data").getLong("cellular_data_limit") * MEGABYTES_TO_BYTES;
        cellPlanDate = config.getJSONObject("data").getInt("normalized_cell_plan_date");

        ObjectMapper mapper = new ObjectMapper();
        dataSent = mapper.readValue(new File("json_file"), JSONObject.class).getLong("usage");
    }

    public static void updateDataSent(int dataSize) {
        dataSent += dataSize;
        writeToUsageFile(dataSent);
    }

    public static Boolean hasNotExceededDataLimit(int dataSize) {
        return (dataSent + dataSize <= cellularDataLimit);
    }

    public static void setUsageFile(String file) throws IOException {
        usageFile = file;
        File usageFile = new File(file);
        if (usageFile.createNewFile()) {
            writeToUsageFile(0L);
        }
    }

    public static void writeToUsageFile(Long usage) {
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

    public static int getBufferSize() {
        return (int)(storageLimit / 5000); // divide by 5KB to get amount of messages
    }
}
