package io.auklet.agent;

import io.auklet.agent.broker.Client;
import io.auklet.agent.broker.MQTTClient;
import io.auklet.agent.broker.SerialClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Auklet {

    protected static String AppId;
    protected static String ApiKey;
    protected static String folderPath;
    protected static Client client;
    private static Logger logger = LoggerFactory.getLogger(Auklet.class);

    private Auklet(){ }

    private static void setup(String appId, String apiKey, boolean handleShutDown, String serialOut) {
        ApiKey = apiKey;
        AppId = appId;

        ScheduledExecutorService mqttThreadPool = createThreadPool();
        createFolderPath();
        SystemMetrics.initSystemMetrics();

        client = createClient(serialOut, mqttThreadPool);

        if (client != null && client.isSetUp()) {
            AukletExceptionHandler.setup();
            if(handleShutDown)
                setUpShutdownThread(mqttThreadPool);
        }
    }

    public static void init(String appId, String apiKey) {
        setup(appId, apiKey, true, "");
    }

    public static void init(String appId, String apiKey, boolean handleShutDown) {
        setup(appId, apiKey, handleShutDown, "");
    }

    public static void init(String appId, String apiKey, boolean handleShutDown, String serialOut) {
        setup(appId, apiKey, handleShutDown, serialOut);
    }

    public static void exception(Throwable thrown) {
        AukletExceptionHandler.sendEvent(thrown);
    }

    private static ScheduledExecutorService createThreadPool() {
        /*
        Ref: https://github.com/eclipse/paho.mqtt.java/issues/402#issuecomment-424686340
         */
        return Executors.newScheduledThreadPool(10,
                (Runnable r) -> {
                    Thread t = Executors.defaultThreadFactory().newThread(r);
                    t.setDaemon(true);
                    return t;
                });
    }

    private static void createFolderPath() {
        folderPath = Util.createCustomFolder("user.dir");
        if (folderPath == null){
            folderPath = Util.createCustomFolder("user.home");
        }
        if (folderPath == null) {
            folderPath = Util.createCustomFolder("java.io.tmpdir");
        }
        logger.info("Directory to store creds: " + folderPath);
    }

    private static Client createClient(String serialOut, ScheduledExecutorService mqttThreadPool) {
        if (serialOut.equals("")) {
            if(Device.get_Certs() && Device.register_device()) {
                return new MQTTClient(AppId, mqttThreadPool);
            }
        } else {
            return new SerialClient(serialOut);
        }
        return null;
    }

    private static void setUpShutdownThread(ScheduledExecutorService mqttThreadPool) {
        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {
                    try {
                        logger.info("Auklet agent shutting down");
                        client.shutdown(mqttThreadPool);
                    } catch (Exception e) {
                        logger.error("Error while shutting down Auklet agent", e);
                    }
                })
        );
    }

    public static String getBaseUrl() {
      String fromEnv = System.getenv("AUKLET_BASEURL");
      return fromEnv != null ? fromEnv : "https://api.auklet.io";
    }

    public static String getFolderPath() {
        return folderPath;
    }
}
