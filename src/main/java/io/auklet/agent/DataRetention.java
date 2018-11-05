package io.auklet.agent;

import org.json.JSONObject;

public class DataRetention {
    static protected Long emissionPeriod;
    static protected Long storageLimit;
    static protected Long celluarDataLimit;
    static protected int cellPlanDate;

    private DataRetention(){ }

    protected static void initDataRetention(JSONObject config) {

    }

    protected static Boolean hasExceededDataLimit(int dataSize) {

    }
}
