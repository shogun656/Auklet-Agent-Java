package io.auklet.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.auklet.AukletException;
import io.auklet.misc.Tls12SocketFactory;
import io.auklet.misc.Util;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.Immutable;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

/**
 * <p>All HTTP requests to the Auklet API are handled by this class.</p>
 *
 * <p>Configuring the SLF4J logger for this class only controls logging statements for internal
 * operations (e.g. shutting down the HTTP client). To configure logging of HTTP requests/responses,
 * you must configure the SLF4J logger named {@code io.auklet.http}. Levels for this logger are handled
 * as follows:</p>
 *
 * <ul>
 *     <li>Set the logger level to {@code TRACE} to log req/resp lines/headers/bodies.</li>
 *     <li>Set the logger level to {@code DEBUG} to log req/resp lines/headers.</li>
 *     <li>Set the logger level to {@code INFO} to log req/resp lines.</li>
 *     <li>Any other logger level disables HTTP logging.</li>
 * </ul>
 *
 * <p>Due to a technical limitation, all messages logged to {@code io.auklet.http} will always be
 * logged at level {@code INFO} (unless logging is disabled), taking into consideration the behavior
 * of the levels described above.</p>
 */
@Immutable
public final class AukletApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(AukletApi.class);
    private static final Logger HTTP_LOGGER = LoggerFactory.getLogger("io.auklet.http");
    private static final HttpLoggingInterceptor INTERCEPTOR = AukletApi.createLogger();
    private final String apiKey;
    @GuardedBy("itself") private final OkHttpClient httpClient;

    /**
     * <p>Constructor.</p>
     *
     * @param apiKey the Auklet API key. Never {@code null} or empty.
     * @param rootCa the root CA Certificate to use. If {@code null}, the truststore
     * provided by the OS/JVM will be used.
     * @throws AukletException if the API key is {@code null} or empty, or if the root CA is not
     * {@code null} and an error occurs while initializing the SSL socket factory.
     */
    public AukletApi(@NonNull String apiKey, @Nullable InputStream rootCa) throws AukletException {
        if (Util.isNullOrEmpty(apiKey)) throw new AukletException("API key is null or empty.");
        this.apiKey = apiKey;
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(AukletApi.INTERCEPTOR);
        AukletApi.setSslSocketFactory(builder, rootCa);
        this.httpClient = builder.build();
    }

    /**
     * <p>Makes an authenticated request to the Auklet API.</p>
     *
     * @param request never {@code null}.
     * @return never {@code null}.
     * @throws AukletException if an error occurs with the request.
     */
    @NonNull public Response doRequest(@NonNull Request.Builder request) throws AukletException {
        if (request == null) throw new AukletException("HTTP request is null.");
        // We handle auth in this method so that the API key does not have
        // to be shared across classes.
        request.header("Authorization", "JWT " + this.apiKey);
        Request req = request.build();
        try {
            synchronized (this.httpClient) {
                return this.httpClient.newCall(req).execute();
            }
        } catch (IOException e) {
            throw new AukletException("Error while making HTTP request.", e);
        }
    }

    /** <p>Shuts down the internal HTTP client.</p> */
    public void shutdown() {
        synchronized (this.httpClient) {
            try {
                this.httpClient.dispatcher().executorService().shutdown();
                this.httpClient.connectionPool().evictAll();
                Cache cache = this.httpClient.cache();
                if (cache != null) cache.close();
            } catch (IOException e) {
                LOGGER.warn("Error while shutting down Auklet API.", e);
            }
        }
    }

    /**
     * <p>Constructs/injects the appropriate SSL socket factory into the OkHttp client.</p>
     *
     * @param builder the OkHttp client builder object, never {@code null}.
     * @param rootCa the root CA Certificate to use. If {@code null}, the truststore
     * provided by the OS/JVM will be used.
     * @throws AukletException if the root CA is not {@code null} and an error occurs while
     * initializing the SSL socket factory.
     */
    private static void setSslSocketFactory(@NonNull OkHttpClient.Builder builder, @Nullable InputStream rootCa) throws AukletException {
        try {
            SSLContext context = SSLContext.getInstance("TLSv1.2");
            KeyStore ca = null;
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            if (rootCa != null) {
                LOGGER.info("Using custom root CA.");
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                Certificate certificate = certificateFactory.generateCertificate(rootCa);
                try {
                    rootCa.close();
                } catch (IOException e) {
                    LOGGER.warn("Error while closing custom root CA InputStream", e);
                }
                ca = KeyStore.getInstance(KeyStore.getDefaultType());
                ca.load(null, null);
                ca.setCertificateEntry("ca-certificate", certificate);
            }
            tmf.init(ca);
            X509TrustManager trustManager = (X509TrustManager) tmf.getTrustManagers()[0];
            context.init(null, trustManager == null ? null : new TrustManager[] {trustManager}, null);
            builder.sslSocketFactory(new Tls12SocketFactory(context), trustManager);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | KeyManagementException e) {
            throw new AukletException("Error while setting up HTTPS SSL socket factory.", e);
        }
    }

    /**
     * <p>Creates an OkHttp logging interceptor that sends all HTTP logs to the SLF4J logger named
     * {@code io.auklet.http}.</p>
     *
     * @return never {@code null}.
     */
    @NonNull private static HttpLoggingInterceptor createLogger() {
        HttpLoggingInterceptor.Level level;
        if (HTTP_LOGGER.isTraceEnabled()) level = HttpLoggingInterceptor.Level.BODY;
        else if (HTTP_LOGGER.isDebugEnabled()) level = HttpLoggingInterceptor.Level.HEADERS;
        else if (HTTP_LOGGER.isInfoEnabled()) level = HttpLoggingInterceptor.Level.BASIC;
        else level = HttpLoggingInterceptor.Level.NONE;
        if (level != HttpLoggingInterceptor.Level.NONE) LOGGER.info("Auklet HTTP request logging is enabled.");
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override public void log(String message) {
                HTTP_LOGGER.info(message);
            }
        });
        logging.setLevel(level);
        return logging;
    }

}
