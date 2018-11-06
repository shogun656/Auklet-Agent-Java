package io.auklet.agent;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Scanner;
import java.util.concurrent.ScheduledExecutorService;

public final class MQTT {

    private MQTT(){ }

    protected static MqttAsyncClient connectMqtt(String folderPath, ScheduledExecutorService executorService){

        JSONObject brokerJSON = getbroker();

        if(brokerJSON != null) {
            String serverUrl = "ssl://" + brokerJSON.get("brokers") + ":" + brokerJSON.get("port");

            MqttAsyncClient client;
            try {
                client = new MqttAsyncClient(serverUrl, Device.getClient_Id(), new MemoryPersistence(),
                        new TimerPingSender(), executorService);
                client.setCallback(getMqttCallback());
                client.setBufferOpts(getDisconnectBufferOptions());

                System.out.println("starting connect the server...");
                client.connect(getMqttConnectOptions(folderPath));
                System.out.println("connected!");

                return client;
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
            }
        }
        return null;
    }

    private static MqttCallback getMqttCallback() {
        return new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("Unexpected disconnect from MQTT"); // Switch to log
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                try {
                    DataRetention.updateDataSent(token.getMessage().getPayload().length);
                    System.out.println("Message published"); // Switch to log
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private static MqttConnectOptions getMqttConnectOptions(String folderPath) {
        String caFilePath = folderPath + "/CA";
        SSLSocketFactory socketFactory = getSocketFactory(caFilePath);
        String mqttUserName = Device.getClient_Username();
        String mqttPassword = Device.getClient_Password();

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

    private static SSLSocketFactory getSocketFactory (String caFilePath){

        try {
            X509Certificate caCert = null;

            FileInputStream fis = new FileInputStream(caFilePath);
            BufferedInputStream bis = new BufferedInputStream(fis);
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
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

        System.out.println("something went wrong while setting up socket factory");

        return null;

    }

    private static JSONObject getbroker(){

        HttpClient httpClient = HttpClientBuilder.create().build();

        try {
            HttpGet request = new HttpGet(Auklet.getBaseUrl() + "/private/devices/config/");
            request.addHeader("content-type", "application/json");
            request.addHeader("Authorization", "JWT " + Auklet.ApiKey);
            HttpResponse response = httpClient.execute(request);

            if (response.getStatusLine().getStatusCode() == 200) {
                String text = null;
                try (Scanner scanner = new Scanner(response.getEntity().getContent(), StandardCharsets.UTF_8.name())) {
                    text = scanner.useDelimiter("\\A").next();
                } catch (Exception e) {
                    System.out.println("Exception occurred during reading brokers info: " + e.getMessage());
                    return null;
                }
                return new JSONObject(text);
            }
            else {
                System.out.println("Get broker response code: " + response.getStatusLine().getStatusCode());
            }

        }catch(Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return null;
    }

}