package io.auklet.agent;

public class Auklet {

    static protected String AppId;
    static protected String ApiKey;
    static protected String baseUrl = "https://api-staging.auklet.io/";

    public static void init(String appId, String apiKey){
        ApiKey = apiKey;
        AppId = appId;
        Device.get_certs();
        Device.register_device();
        AukletUnhandledException.setup();
    }
}
