package io.auklet.agent;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Auklet {

    protected static String appId;
    protected static String apiKey;
    protected static String folderPath;
    protected static MqttAsyncClient client;
    private static Logger logger = LoggerFactory.getLogger(Auklet.class);

    /*
    Ref: https://github.com/eclipse/paho.mqtt.java/issues/402#issuecomment-424686340
     */
    private static ScheduledExecutorService mqttThreadPool = Executors.newScheduledThreadPool(10,
            (Runnable r) -> {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(true);
                return t;
            });

    private Auklet(){ }

    private static void setup(String appId, String apiKey, boolean handleShutDown){
        Auklet.apiKey = apiKey;
        Auklet.appId = appId;

        if(handleShutDown) {
            Runtime.getRuntime().addShutdownHook(
                    new Thread(() -> {
                        try {
                            logger.info("Auklet agent shutting down");
                            Auklet.shutdown();
                        } catch (Exception e) {
                            logger.error("Error while shutting down Auklet agent", e);
                        }
                    })
            );
        }

        SystemMetrics.initSystemMetrics();

        folderPath = Util.createCustomFolder("user.dir");
        if (folderPath == null) {
            folderPath = Util.createCustomFolder("user.home");
        }
        if (folderPath == null) {
            folderPath = Util.createCustomFolder("java.io.tmpdir");
        }
        logger.info("Directory to store creds: {}", folderPath);

        if(Device.registerDevice() && Device.getCerts() && Device.initLimitsConfig()) {
            client = MQTT.connectMqtt(mqttThreadPool);
            if (client != null) {
                AukletExceptionHandler.setup();
            }
        }
    }

    public static void init(String appId, String apiKey){
        setup(appId, apiKey, true);
    }

    public static void init(String appId, String apiKey, boolean handleShutDown){
        setup(appId, apiKey, handleShutDown);
    }

    public static void exception(Throwable thrown){
        AukletExceptionHandler.sendEvent(thrown);
    }

    public static void shutdown() {
        if (client.isConnected()) {
            try {
                client.disconnect().waitForCompletion();
            } catch (MqttException e) {
                logger.error("Error while disconnecting MQTT client", e);
                try {
                    client.disconnectForcibly();
                } catch (MqttException e2) {
                    // No reason to log this, since we already failed to disconnect once.
                }
            }
        }
        try {
            client.close();
        } catch (MqttException e) {
            logger.error("Error while closing MQTT client", e);
        } finally {
            mqttThreadPool.shutdown();
            try {
                mqttThreadPool.awaitTermination(3, TimeUnit.SECONDS);
            } catch (InterruptedException e2) {
                // End users that call shutdown() explicitly should only do so inside the context of a JVM shutdown.
                // Thus, rethrowing this exception creates unnecessary noise and clutters the API/Javadocs.
                logger.warn("Interrupted while awaiting MQTT thread pool shutdown", e2);
                Thread.currentThread().interrupt();
            }
        }
    }

    public static String getBaseUrl() {
      String fromEnv = System.getenv("AUKLET_BASEURL");
      return fromEnv != null ? fromEnv : "https://api.auklet.io";
    }

}
