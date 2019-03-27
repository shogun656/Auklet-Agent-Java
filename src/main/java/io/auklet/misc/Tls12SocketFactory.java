package io.auklet.misc;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.auklet.AukletException;
import net.jcip.annotations.NotThreadSafe;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * <p>A custom SSL socket factory that only supports TLS 1.2. This class adds compatibility for SSL connections
 * on Android versions less than 4.4W.</p>
 *
 * <p>Derived from https://gist.githubusercontent.com/fkrauthan/ac8624466a4dee4fd02f/raw/309efc30e31c96a932ab9d19bf4d73b286b00573/TLSSocketFactory.java.</p>
 *
 * @see <a href="https://developer.android.com/reference/javax/net/ssl/SSLSocket.html">SSLSocket docs for Android</a>
 */
@NotThreadSafe
public final class Tls12SocketFactory extends SSLSocketFactory {

    private final SSLSocketFactory delegateFactory;

    /**
     * <p>Constructor that uses the provided SSL context.</p>
     *
     * @param context if {@code null}, a default SSL context will be used.
     * @throws AukletException if the context cannot be created, or if the socket factory cannot be retrieved.
     */
    public Tls12SocketFactory(@Nullable SSLContext context) throws AukletException {
        try {
            if (context == null) {
                context = SSLContext.getInstance("TLS");
                context.init(null, null, null);
            }
            delegateFactory = context.getSocketFactory();
        } catch (KeyManagementException | NoSuchAlgorithmException | IllegalStateException e) {
            throw new AukletException("Could not initialize SSL socket factory.", e);
        }
    }

    @Override public String[] getDefaultCipherSuites() {
        return delegateFactory.getDefaultCipherSuites();
    }

    @Override public String[] getSupportedCipherSuites() {
        return delegateFactory.getSupportedCipherSuites();
    }

    @Override public Socket createSocket() throws IOException {
        return setSocketOnlyTls12(delegateFactory.createSocket());
    }

    @Override public Socket createSocket(@NonNull Socket s, @Nullable String host, int port, boolean autoClose) throws IOException {
        return setSocketOnlyTls12(delegateFactory.createSocket(s, host, port, autoClose));
    }

    @Override public Socket createSocket(@Nullable String host, int port) throws IOException {
        return setSocketOnlyTls12(delegateFactory.createSocket(host, port));
    }

    @Override public Socket createSocket(@Nullable String host, int port, @Nullable InetAddress localHost, int localPort) throws IOException {
        return setSocketOnlyTls12(delegateFactory.createSocket(host, port, localHost, localPort));
    }

    @Override public Socket createSocket(@NonNull InetAddress host, int port) throws IOException {
        return setSocketOnlyTls12(delegateFactory.createSocket(host, port));
    }

    @Override public Socket createSocket(@NonNull InetAddress address, int port, @Nullable InetAddress localAddress, int localPort) throws IOException {
        return setSocketOnlyTls12(delegateFactory.createSocket(address, port, localAddress, localPort));
    }

    private Socket setSocketOnlyTls12(@Nullable Socket socket) {
        if (socket instanceof SSLSocket) ((SSLSocket) socket).setEnabledProtocols(new String[]{"TLSv1.2"});
        return socket;
    }

}
