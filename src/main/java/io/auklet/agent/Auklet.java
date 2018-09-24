package io.auklet.agent;

public class Auklet {

    static protected String AppId;
    static protected String ApiKey;
    static protected String baseUrl = "https://auklet.io/";

    public static void init(String appId, String apiKey){
        ApiKey = apiKey;
        AppId = appId;
    }
}
