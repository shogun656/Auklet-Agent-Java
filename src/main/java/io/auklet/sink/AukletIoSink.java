package io.auklet.sink;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
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
     * @throws AukletException if the underlying MQTT client cannot be constructed or started, or if
     * the SSL cert/broker config cannot be obtained.
     */
    @Override
    public void setAgent(@NonNull Auklet agent) throws AukletException {
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
    public void write(@Nullable byte[] bytes) throws AukletException {
        try {
            MqttMessage message = new MqttMessage(bytes);
            // TODO does MqttMessage add more data beyond what's in the bytes array?
            // if so, we need to change how we report/track data usage
            message.setQos(1);
            client.publish(this.getAgent().getDeviceAuth().getMqttEventsTopic(), message);
        } catch (MqttException e) {
            throw new AukletException("Error while publishing MQTT message", e);
        }
    }

    @Override
    public void shutdown() throws AukletException {
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

    /**
     * <p>Returns the MQTT callback object used by the MQTT client.</p>
     *
     * @return never {@code null}.
     */
    @NonNull private MqttCallback getCallback() {
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

    /**
     * <p>Returns the MQTT disconnected buffer options object.</p>
     *
     * @param agent the Auklet agent reference. Never {@code null}.
     * @return never {@code null}.
     * @throws AukletException if the options object cannot be constructed, or if any argument is {@code null}.
     */
    @NonNull private DisconnectedBufferOptions getDisconnectBufferOptions(@NonNull Auklet agent) throws AukletException {
        if (agent == null) throw new AukletException("Auklet agent is null");
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

    /**
     * <p>Returns the MQTT connect options object.</p>
     *
     * @param agent the Auklet agent reference. Never {@code null}.
     * @param cert the Auklet SSL certificate object. Never {@code null}.
     * @return never {@code null}.
     * @throws AukletException if the options object cannot be constructed, or if any argument is {@code null}.
     */
    @NonNull private MqttConnectOptions getConnectOptions(@NonNull Auklet agent, @NonNull X509Certificate cert) throws AukletException {
        if (agent == null) throw new AukletException("Auklet agent is null");
        if (cert == null) throw new AukletException("Auklet SSL cert is null");
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

    /**
     * <p>Returns the MQTT SSL socket factory.</p>
     *
     * @param cert the Auklet SSL certificate object. Never {@code null}.
     * @return never {@code null}.
     * @throws AukletException if the factory cannot be constructed, or if any argument is {@code null}.
     */
    @NonNull private SSLSocketFactory getSocketFactory(@NonNull X509Certificate cert) throws AukletException {
        if (cert == null) throw new AukletException("Auklet SSL cert is null");
        try {
            KeyStore ca = KeyStore.getInstance(KeyStore.getDefaultType());
            ca.load(null, null);
            ca.setCertificateEntry("ca-certificate", cert);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(ca);
            SSLContext context = SSLContext.getInstance("TLSv1.2");
            context.init(null, tmf.getTrustManagers(), null);
            return context.getSocketFactory();
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | KeyManagementException e) {
            throw new AukletException("Error while setting up MQTT SSL socket factory", e);
        }
    }

    /** <p>Shuts down the MQTT client's thread pool.</p> */
    private void shutdownThreadPool() {
        this.executorService.shutdown();
        try {
            this.executorService.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e2) {
            // End-users that call shutdown() explicitly should only do so inside the context of a JVM shutdown.
            // Thus, rethrowing this exception creates unnecessary noise and clutters the API/Javadocs.
            LOGGER.warn("Interrupted while awaiting MQTT thread pool shutdown", e2);
            Thread.currentThread().interrupt();
        }
    }

}
