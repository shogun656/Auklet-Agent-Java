package io.auklet.net;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import net.jcip.annotations.NotThreadSafe;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * <p>A custom SSL socket factory that only supports TLS 1.2. This class adds compatibility for SSL connections
 * on Android versions less than 4.4W, but is safe to use in Android 4.4W+ and non-Android environments.</p>
 *
 * <p>Derived from https://gist.githubusercontent.com/fkrauthan/ac8624466a4dee4fd02f/raw/309efc30e31c96a932ab9d19bf4d73b286b00573/TLSSocketFactory.java.</p>
 *
 * @see <a href="https://developer.android.com/reference/javax/net/ssl/SSLSocket.html">SSLSocket docs for Android</a>
 */
@NotThreadSafe
public final class Tls12SocketFactory extends SSLSocketFactory {

    private final SSLSocketFactory delegateFactory;

    /**
     * <p>Constructor that uses the provided SSL context. This context must already be initialized.</p>
     *
     * @param context the SSL context whose socket factory will be wrapped.
     * @throws IllegalArgumentException if the SSL context is {@code null}.
     */
    public Tls12SocketFactory(@NonNull SSLContext context) {
        if (context == null) throw new IllegalArgumentException("SSL context is null.");
        delegateFactory = context.getSocketFactory();
    }

    @Override public String[] getDefaultCipherSuites() {
        return delegateFactory.getDefaultCipherSuites();
    }

    @Override public String[] getSupportedCipherSuites() {
        return delegateFactory.getSupportedCipherSuites();
    }

    @Override public Socket createSocket() throws IOException {
        return tls12Only(delegateFactory.createSocket());
    }

    @Override public Socket createSocket(@NonNull Socket s, @Nullable String host, int port, boolean autoClose) throws IOException {
        return tls12Only(delegateFactory.createSocket(s, host, port, autoClose));
    }

    @Override public Socket createSocket(@Nullable String host, int port) throws IOException {
        return tls12Only(delegateFactory.createSocket(host, port));
    }

    @Override public Socket createSocket(@Nullable String host, int port, @Nullable InetAddress localHost, int localPort) throws IOException {
        return tls12Only(delegateFactory.createSocket(host, port, localHost, localPort));
    }

    @Override public Socket createSocket(@NonNull InetAddress host, int port) throws IOException {
        return tls12Only(delegateFactory.createSocket(host, port));
    }

    @Override public Socket createSocket(@NonNull InetAddress address, int port, @Nullable InetAddress localAddress, int localPort) throws IOException {
        return tls12Only(delegateFactory.createSocket(address, port, localAddress, localPort));
    }

    @CheckForNull private Socket tls12Only(@Nullable Socket socket) {
        if (socket == null) throw new IllegalArgumentException("Socket is null.");
        if (socket instanceof SSLSocket) {
            SSLSocket sslsocket = (SSLSocket) socket;
            sslsocket.setEnabledProtocols(new String[]{"TLSv1.2"});
        }
        return socket;
    }

}
