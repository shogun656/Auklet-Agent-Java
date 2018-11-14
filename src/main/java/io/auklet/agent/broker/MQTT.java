package io.auklet.agent.broker;

import io.auklet.agent.Auklet;
import io.auklet.agent.Device;
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
import java.util.concurrent.TimeUnit;

public class MQTT implements Client {

    private MqttClient client;

    public MQTT(String appId, String folderPath, ScheduledExecutorService executorService) {
        client = connectMqtt(appId, folderPath, executorService);
    }

    private MqttClient connectMqtt(String appId, String folderPath, ScheduledExecutorService executorService) {
        JSONObject brokerJSON = getbroker(appId);

        if(brokerJSON != null) {
            String serverUrl = "ssl://" + brokerJSON.getString("brokers") + ":" + brokerJSON.getString("port");
            String caFilePath = folderPath + "/CA";
            String mqttUserName = Device.getClient_Username();
            String mqttPassword = Device.getClient_Password();

            MqttClient client;
            try {
                client = new MqttClient(serverUrl, Device.getClient_Id(), new MemoryPersistence(), executorService);
                MqttConnectOptions options = new MqttConnectOptions();
                options.setUserName(mqttUserName);
                options.setPassword(mqttPassword.toCharArray());

                options.setConnectionTimeout(60);
                options.setKeepAliveInterval(60);
                options.setCleanSession(true);
                options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);

                SSLSocketFactory socketFactory = getSocketFactory(caFilePath);
                options.setSocketFactory(socketFactory);

                System.out.println("starting connect the server...");
                client.connect(options);
                System.out.println("connected!");

                return client;
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
            }
        }
        return null;
    }

    private SSLSocketFactory getSocketFactory (String caFilePath) {
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

    private JSONObject getbroker(String appId) {
        HttpClient httpClient = HttpClientBuilder.create().build();

        try {
            HttpGet request = new HttpGet(Auklet.getBaseUrl() + "/private/devices/config/");
            request.addHeader("content-type", "application/json");
            request.addHeader("Authorization", "JWT " + appId);
            HttpResponse response = httpClient.execute(request);

            if (response.getStatusLine().getStatusCode() == 200) {
                String text;
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

    @Override
    public void sendEvent(byte[] bytesToSend) {
        try {
            MqttMessage message = new MqttMessage(bytesToSend);
            message.setQos(1);
            client.publish("java/events/" + Device.getOrganization() + "/" +
                    Device.getClient_Username(), message);
            System.out.println("Message published");

        } catch (MqttException | NullPointerException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }


    @Override
    public void shutdown(ScheduledExecutorService threadPool) {
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
            threadPool.shutdown();
            try {
                threadPool.awaitTermination(3, TimeUnit.SECONDS);
            } catch (InterruptedException e2) {}
        }
    }
}