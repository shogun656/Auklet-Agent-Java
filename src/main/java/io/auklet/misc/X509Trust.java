package io.auklet.misc;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.auklet.AukletException;
import net.jcip.annotations.NotThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <p>A data structure that contains an X.509-baesd TLSv1.2 SSL context and corresponding
 * SSL trust manager. This class encapsulates the use case where only a finite set of
 * certificates can be trusted, though this class can also be used when this is not
 * the case and the default truststore provided by the OS/JVM is desired.</p>
 */
@NotThreadSafe
public final class X509Trust {

    private static final Logger LOGGER = LoggerFactory.getLogger(X509Trust.class);
    private final SSLContext context;
    private final X509TrustManager trustManager;

    /**
     * <p>Constructor.</p>
     *
     * @param certificates the SSL certificates to use. If {@code null} or empty,
     * the truststore provided by the OS/JVM will be used.
     * @throws AukletException if any error occurs while parsing the certificates, or while
     * creating the trust manager or the SSL context.
     */
    private X509Trust(@Nullable Collection<X509Certificate> certificates) throws AukletException {
        try {
            // Setup the truststore, if custom SSL certificates were provided.
            // Otherwise, we use the default JVM-or-OS-provided truststore.
            KeyStore ca = null;
            if (!Util.isNullOrEmpty(certificates)) {
                StringBuilder certLog = new StringBuilder("Using custom SSL certificates:\n");
                DateFormat df = DateFormat.getDateTimeInstance();
                ca = KeyStore.getInstance(KeyStore.getDefaultType());
                ca.load(null, null);
                int i = 0;
                for (X509Certificate cert : certificates) {
                    certLog.append('#').append(i).append(":\n")
                            .append("- Subject: ").append(cert.getSubjectX500Principal().getName()).append('\n')
                            .append("- Issuer: ").append(cert.getIssuerX500Principal().getName()).append('\n')
                            .append("- From: ").append(df.format(cert.getNotBefore())).append('\n')
                            .append("- To: ").append(df.format(cert.getNotAfter())).append('\n');
                    ca.setCertificateEntry(Integer.toString(i++), cert);
                }
                LOGGER.info(certLog.toString().trim());
            }
            // Setup the trust manager using the truststore, and make sure it is X.509.
            // The trust manager is needed by both OkHttp directly and by the SSL context (which provides
            // the socket factory used by OkHttp).
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(ca);
            TrustManager[] tms = tmf.getTrustManagers();
            TrustManager tm = tms[0];
            if (!(tm instanceof X509TrustManager))
                throw new AukletException("TrustManager is not X509TrustManager, but is instead " + tm.getClass().getName());
            trustManager = (X509TrustManager) tm;
            // Define and initialize the context.
            context = SSLContext.getInstance("TLSv1.2");
            context.init(null, new TrustManager[]{tm}, null);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | KeyManagementException e) {
            throw new AukletException("Could not create X.509 trust object.", e);
        }
    }

    /**
     * <p>Creates an X.509 trust object from a list of input streams of X.509 certificates
     * that are to form the truststore. These input streams will be closed by this method.</p>
     *
     * @param certificates the SSL certificates to use. If {@code null} or empty,
     * the truststore provided by the OS/JVM will be used.
     * @throws AukletException if any error occurs while parsing the certificates, or while
     * creating the trust manager or the SSL context.
     */
    @NonNull public static X509Trust fromStreams(@Nullable Collection<InputStream> certificates) throws AukletException {
        if (Util.isNullOrEmpty(certificates)) return new X509Trust(null);
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            List<X509Certificate> certList = new ArrayList<>(certificates.size());
            for (InputStream cert : certificates) {
                certList.add((X509Certificate) certificateFactory.generateCertificate(cert));
                Util.closeQuietly(cert);
            }
            return new X509Trust(certList);
        } catch (CertificateException e) {
            throw new AukletException("Could not create X.509 trust object.", e);
        }
    }

    /**
     * <p>Creates an X.509 trust object from a list of X.509 certificate objects that are
     * to form the truststore. These input streams will be closed by this method.</p>
     *
     * @param certificates the SSL certificates to use. If {@code null} or empty,
     * the truststore provided by the OS/JVM will be used.
     * @throws AukletException if any error occurs while parsing the certificates, or while
     * creating the trust manager or the SSL context.
     */
    @NonNull public static X509Trust fromCerts(@Nullable Collection<X509Certificate> certificates) throws AukletException {
        return new X509Trust(certificates);
    }

    /**
     * <p>Returns the trust manager.</p>
     *
     * @return never {@code null}.
     */
    @NonNull public X509TrustManager getTrustManager() { return trustManager; }

    /**
     * <p>Creates a returns a TLSv1.2-only SSL socket factory.</p>
     *
     * @return never {@code null}.
     */
    @NonNull public Tls12SocketFactory createSocketFactory() { return new Tls12SocketFactory(context); }

}
