package io.auklet.sink;

import io.auklet.Auklet;
import io.auklet.AukletException;
import io.auklet.config.AukletIoBrokers;
import io.auklet.config.AukletIoCert;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** The default Auklet data sink, which sends data to {@code auklet.io} via MQTT. */
public final class AukletIoSink extends AbstractSink {

    private static final Logger LOGGER = LoggerFactory.getLogger(AukletIoSink.class);
    private ScheduledExecutorService executorService;
    private MqttAsyncClient client;

    /**
     * <p>Constructs the underlying MQTT client.</p>
     *
     * @throws AukletException if the underlying MQTT client cannot be constructed or started.
     */
    @Override
    public void setAgent(Auklet agent) throws AukletException {
        super.setAgent(agent);
        try {
            AukletIoCert cert = new AukletIoCert();
            cert.setAgent(agent);
            AukletIoBrokers brokers = new AukletIoBrokers();
            brokers.setAgent(agent);
            // Workaround to ensure that MQTT client threads do not stop JVM shutdown.
            // https://github.com/eclipse/paho.mqtt.java/issues/402#issuecomment-424686340
            this.executorService = Executors.newScheduledThreadPool(10,
                    (Runnable r) -> {
                        Thread t = Executors.defaultThreadFactory().newThread(r);
                        t.setDaemon(true);
                        return t;
                    });
            this.client = new MqttAsyncClient(brokers.getUrl(), agent.getDeviceAuth().getClientId(), new MemoryPersistence(), new TimerPingSender(), executorService);
            this.client.setCallback(this.getCallback());
            this.client.setBufferOpts(this.getDisconnectBufferOptions(agent));
            this.client.connect(this.getConnectOptions(agent, cert.getCert()));
        } catch (MqttException e) {
            this.shutdownThreadPool();
            throw new AukletException("Could not initialize MQTT sink", e);
        }
    }

    @Override
    public void write(byte[] bytes) throws SinkException {
        try {
            MqttMessage message = new MqttMessage(bytes);
            // TODO does MqttMessage add more data beyond what's in the bytes array?
            // if so, we need to change how we report/track data usage
            message.setQos(1);
            client.publish(this.getAgent().getDeviceAuth().getMqttEventsTopic(), message);
        } catch (AukletException | MqttException e) {
            throw new SinkException("Error while publishing MQTT message", e);
        }
    }

    @Override
    public void shutdown() throws SinkException {
        super.shutdown();
        if (this.client.isConnected()) {
            try {
                this.client.disconnect().waitForCompletion();
            } catch (MqttException e) {
                LOGGER.warn("Error while disconnecting MQTT client", e);
                try {
                    this.client.disconnectForcibly();
                } catch (MqttException e2) {
                    // No reason to log this, since we already failed to disconnect once.
                }
            }
        }
        try {
            this.client.close();
        } catch (MqttException e) {
            LOGGER.error("Error while shutting down MQTT client", e);
        } finally {
            this.shutdownThreadPool();
        }
    }

    private MqttCallback getCallback() {
        return new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                LOGGER.error("Unexpected disconnect from MQTT");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                // TODO: handle messages received from the Auklet backend.
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // TODO: handle what happens when MQTT message delivery completes
            }
        };
    }

    private DisconnectedBufferOptions getDisconnectBufferOptions(Auklet agent) {
        // Divide by 5KB to get amount of messages.
        long storageLimit = agent.getUsageLimit().getStorageLimit();
        int bufferSize = (storageLimit == 0) ? 5000 : (int) storageLimit / 5000;
        DisconnectedBufferOptions disconnectOptions = new DisconnectedBufferOptions();
        disconnectOptions.setBufferEnabled(true);
        disconnectOptions.setDeleteOldestMessages(true);
        disconnectOptions.setPersistBuffer(true);
        disconnectOptions.setBufferSize(bufferSize);
        return disconnectOptions;
    }

    private MqttConnectOptions getConnectOptions(Auklet agent, X509Certificate cert) throws AukletException {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(agent.getDeviceAuth().getClientUsername());
        options.setPassword(agent.getDeviceAuth().getClientPassword().toCharArray());
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
        options.setConnectionTimeout(60);
        options.setKeepAliveInterval(60);
        options.setCleanSession(false);
        options.setAutomaticReconnect(true);
        options.setSocketFactory(this.getSocketFactory(cert));
        return options;
    }

    private SSLSocketFactory getSocketFactory(X509Certificate cert) throws AukletException {
        try {
            // Prepare a keystore that contains the CA cert.
            KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
            caKs.load(null, null);
            caKs.setCertificateEntry("ca-certificate", cert);
            // Define and return the SSL context.
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(caKs);
            SSLContext context = SSLContext.getInstance("TLSv1.2");
            context.init(null, tmf.getTrustManagers(), null);
            // Return a socket factory from this context.
            return context.getSocketFactory();
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | KeyManagementException e) {
            throw new AukletException("Error while setting up MQTT SSL socket factory", e);
        }
    }

    private void shutdownThreadPool() {
        this.executorService.shutdown();
        try {
            this.executorService.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e2) {
            // End users that call shutdown() explicitly should only do so inside the context of a JVM shutdown.
            // Thus, rethrowing this exception creates unnecessary noise and clutters the API/Javadocs.
            LOGGER.warn("Interrupted while awaiting MQTT thread pool shutdown", e2);
            Thread.currentThread().interrupt();
        }
    }

}
