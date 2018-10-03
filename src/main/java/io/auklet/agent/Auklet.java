package io.auklet.agent;

import org.eclipse.paho.client.mqttv3.MqttClient;

public class Auklet {

    static protected String AppId;
    static protected String ApiKey;
    static protected String baseUrl = "https://api-staging.auklet.io/";
    static protected MqttClient client;

    public static void init(String appId, String apiKey){
        ApiKey = apiKey;
        AppId = appId;

        String folderPath = util.createCustomFolder("user.dir");
        if (folderPath == null){
           folderPath = util.createCustomFolder("user.home");
        }
        if (folderPath == null){
            folderPath = util.createCustomFolder("java.io.tmpdir");
        }
        System.out.println("Directory to store creds: " + folderPath);
        Device.get_certs(folderPath);
        Device.register_device(folderPath);
        client = MQTT.connectMqtt(folderPath);
        AukletExceptionHandler.setup();
    }
}
