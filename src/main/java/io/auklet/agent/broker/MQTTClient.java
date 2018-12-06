package io.auklet.agent.broker;

import io.auklet.agent.Auklet;
import io.auklet.agent.DataRetention;
import io.auklet.agent.Device;
import io.auklet.agent.Util;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MQTTClient implements Client {

    private static Logger logger = LoggerFactory.getLogger(MQTTClient.class);
    private MqttAsyncClient client;
    private ScheduledExecutorService executorService;

    public MQTTClient(String apiKey) throws MqttException {
        client = connectMqtt(apiKey);
        if (client == null) {
            throw new NullPointerException();
        }
    }

    private MqttAsyncClient connectMqtt(String apiKey) throws MqttException {
        JSONObject brokerJSON = getBroker(apiKey);

        if(brokerJSON != null) {
            String serverUrl = "ssl://" + brokerJSON.get("brokers") + ":" + brokerJSON.get("port");

            executorService = createThreadPool();
            MqttAsyncClient mqttAsyncClient = new MqttAsyncClient(serverUrl, Device.getClientId(), new MemoryPersistence(),
                    new TimerPingSender(), executorService);
            mqttAsyncClient.setCallback(getMqttCallback());
            mqttAsyncClient.setBufferOpts(getDisconnectBufferOptions());

            logger.info("Auklet starting connect the MQTT server...");
            mqttAsyncClient.connect(getMqttConnectOptions());
            logger.info("Auklet MQTT client connected!");

            return mqttAsyncClient;
        }
        return null;
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

    private static MqttCallback getMqttCallback() {
        return new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                logger.error("Unexpected disconnect from MQTT");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                // TODO: handle messages received from the Auklet backend.
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // TODO: handle what happens when MQTT message delivery completes
            }
        };
    }

    private static MqttConnectOptions getMqttConnectOptions() {
        String caFilePath = Auklet.getFolderPath() + "/CA";
        SSLSocketFactory socketFactory = getSocketFactory(caFilePath);
        String mqttUserName = Device.getClientUsername();
        String mqttPassword = Device.getClientPassword();

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(mqttUserName);
        options.setPassword(mqttPassword.toCharArray());

        options.setConnectionTimeout(60);
        options.setKeepAliveInterval(60);
        options.setCleanSession(false);
        options.setAutomaticReconnect(true);

        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
        options.setSocketFactory(socketFactory);

        return options;
    }

    private static DisconnectedBufferOptions getDisconnectBufferOptions() {
        DisconnectedBufferOptions disconnectOptions = new DisconnectedBufferOptions();
        disconnectOptions.setBufferEnabled(true);
        disconnectOptions.setDeleteOldestMessages(true);
        disconnectOptions.setPersistBuffer(true);
        disconnectOptions.setBufferSize(DataRetention.getBufferSize());
        return disconnectOptions;
    }

    private static SSLSocketFactory getSocketFactory (String caFilePath) {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(caFilePath))) {
            X509Certificate caCert = null;

            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            while (bis.available() > 0) {
                caCert = (X509Certificate) cf.generateCertificate(bis);
            }

            KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
            caKs.load(null, null);
            caKs.setCertificateEntry("ca-certificate", caCert);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(caKs);

            SSLContext context = SSLContext.getInstance("TLSv1.2");
            context.init(null, tmf.getTrustManagers(), null);

            return context.getSocketFactory();
        } catch (Exception e) {
            logger.error("Error while setting up MQTT socket factory", e);
        }

        logger.error("Auklet MQTT Socket factory is null");

        return null;
    }

    private JSONObject getBroker(String apiKey) {
        HttpClient httpClient = HttpClientBuilder.create().build();

        try {
            HttpGet request = new HttpGet(Auklet.getBaseUrl() + "/private/devices/config/");
            request.addHeader("content-type", "application/json");
            request.addHeader("Authorization", "JWT " + apiKey);
            HttpResponse response = httpClient.execute(request);

            String contents = Util.readContents(response);

            if (response.getStatusLine().getStatusCode() == 200 && contents != null) {
                return new JSONObject(contents);
            } else {
                logger.error("Error while getting brokers: {}: {}",
                        response.getStatusLine(), contents);
            }
        } catch(Exception e) {
            logger.error("Error while getting the brokers", e);
        }
        return null;
    }

    @Override
    public void sendEvent(String topic, byte[] bytesToSend) {
        try {
            if (DataRetention.hasNotExceededDataLimit(bytesToSend.length)) {
                MqttMessage message = new MqttMessage(bytesToSend);
                message.setQos(1); // At Least Once Semantics
                client.publish("java/events/" + Device.getOrganization() + "/" +
                        Device.getClientUsername(), message);
                DataRetention.updateDataSent(message.getPayload().length);
                logger.info("Duplicate message published: {}", message.isDuplicate());
            }
        } catch (MqttException | NullPointerException e) {
            logger.error("Error while publishing the MQTT message", e);
        }
    }

    @Override
    public void shutdown() {
        if (client.isConnected()) {
            try {
                client.disconnect().waitForCompletion();
            } catch (MqttException e) {
                logger.error("Error while disconnecting down MQTT agent", e);
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
            logger.error("Error while closing down MQTT Client", e);
        } finally {
            executorService.shutdown();
            try {
                executorService.awaitTermination(3, TimeUnit.SECONDS);
            } catch (InterruptedException e2) {
                // End users that call shutdown() explicitly should only do so inside the context of a JVM shutdown.
                // Thus, rethrowing this exception creates unnecessary noise and clutters the API/Javadocs.
                logger.warn("Interrupted while awaiting MQTT thread pool shutdown", e2);
                Thread.currentThread().interrupt();
            }
        }
    }
}