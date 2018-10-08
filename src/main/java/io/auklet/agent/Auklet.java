package io.auklet.agent;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public final class Auklet {

    static protected String AppId;
    static protected String ApiKey;
    static protected String baseUrl = "https://api-staging.auklet.io/";
    static protected MqttClient client;
    static private Timer timer = new Timer(true);

    /*
    Ref: https://github.com/eclipse/paho.mqtt.java/issues/402#issuecomment-424686340
     */
    static private ScheduledExecutorService mqttThreadPool = Executors.newScheduledThreadPool(10,
            new ThreadFactory() {
                public Thread newThread(Runnable r) {
                    Thread t = Executors.defaultThreadFactory().newThread(r);
                    t.setDaemon(true);
                    return t;
                }
            });

    private Auklet(){ }

    private static void setup(String appId, String apiKey, boolean handleShutDown){
        ApiKey = apiKey;
        AppId = appId;

        if(handleShutDown) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        System.out.println("Auklet agent shutting down");
                        Auklet.shutdown();
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            });
        }

        SystemMetrics.initSystemMetrics();
        SystemMetrics.updateSystemMetric(timer);

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
        client = MQTT.connectMqtt(folderPath, mqttThreadPool);
        AukletExceptionHandler.setup();
    }

    public static void init(String appId, String apiKey){
        setup(appId, apiKey, true);
    }

    public static void init(String appId, String apiKey, boolean handleShutDown){
        setup(appId, apiKey, handleShutDown);
    }

    public static void shutdown(){
        if (client.isConnected()) {
            try {
                client.disconnect();
            } catch (MqttException e) {
                System.out.println(e.getMessage());
                try {
                    client.disconnectForcibly();
                } catch (MqttException e2) {
                }
            }
        }
        try {
            client.close();
        } catch (MqttException e) {
            System.out.println(e.getMessage());
        } finally {
            mqttThreadPool.shutdown();
            try {
                mqttThreadPool.awaitTermination(3, TimeUnit.SECONDS);
            } catch (InterruptedException e2) {
            }
        }
    }

}
