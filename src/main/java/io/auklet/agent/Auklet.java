package io.auklet.agent;

import io.auklet.agent.broker.Client;
import io.auklet.agent.broker.MQTTClient;
import io.auklet.agent.broker.SerialClient;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;

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

        createFolderPath();
        SystemMetrics.initSystemMetrics();

        client = createClient(serialOut);

        if (client != null) {
            AukletExceptionHandler.setup();
            if(handleShutDown)
                setUpShutdownThread();
        }
    }

    public static void init(String appId, String apiKey) {
        setup(appId, apiKey, true, "");
    }

    public static void init(String appId, String apiKey, boolean handleShutDown) {
        setup(appId, apiKey, handleShutDown, "");
    }

    public static void init(String appId, String apiKey, String serialOut) {
        setup(appId, apiKey, true, serialOut);
    }

    public static void init(String appId, String apiKey, boolean handleShutDown, String serialOut) {
        setup(appId, apiKey, handleShutDown, serialOut);
    }

    public static void exception(Throwable thrown) {
        AukletExceptionHandler.sendEvent(thrown);
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

    private static Client createClient(String serialOut) {
        Client client = null;
        if (serialOut.equals("")) {
            if(Device.get_Certs() && Device.initLimitsConfig() && Device.register_device()) {
                try {
                    client = new MQTTClient(AppId);
                } catch (Exception e) {
                    logger.error("MQTTClient is not able to be initialized", e);
                }
            }
        } else {
            try {
                client = new SerialClient(serialOut);
            } catch (NoSuchPortException | PortInUseException | IOException e) {
                logger.error("SerialClient is not able to be initialized", e);
            }
        }
        return client;
    }

    private static void setUpShutdownThread() {
        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {
                    try {
                        logger.info("Auklet agent shutting down");
                        client.shutdown();
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
