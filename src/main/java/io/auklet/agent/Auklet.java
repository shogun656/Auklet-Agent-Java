package io.auklet.agent;

import org.eclipse.paho.client.mqttv3.MqttClient;

public final class Auklet {

    static protected String AppId;
    static protected String ApiKey;
    static protected String baseUrl = "https://api-staging.auklet.io/";
    static protected MqttClient client;

    public static void init(String appId, String apiKey){
        ApiKey = apiKey;
        AppId = appId;

        String folderPath = Util.createCustomFolder("user.dir");
        if (folderPath == null){
           folderPath = Util.createCustomFolder("user.home");
        }
        if (folderPath == null){
            folderPath = Util.createCustomFolder("java.io.tmpdir");
        }
        System.out.println("Directory to store creds: " + folderPath);
        Device.get_Certs(folderPath);
        Device.register_device(folderPath);
        client = MQTT.connectMqtt(folderPath);
        AukletExceptionHandler.setup();
    }
}
