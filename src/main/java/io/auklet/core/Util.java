package io.auklet.core;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/** <p>Utility methods.</p> */
public final class Util {

    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);
    private static final String UNKNOWN_VALUE = "unknown";

    private Util() {}

    /**
     * <p>Returns whether or not a string is either null or empty.</p>
     *
     * @param s the string to check.
     * @return {@code s == null || s.isEmpty()}
     */
    public static boolean isNullOrEmpty(@Nullable String s) {
        return s == null || s.isEmpty();
    }

    /**
     * <p>Returns the first argument if it is not {@code null}, else returns the second argument.</p>
     *
     * @param a the first argument.
     * @param b the second argument.
     * @return {@code a == null ? b : a}.
     */
    @CheckForNull public static String defaultValue(@Nullable String a, @Nullable String b) {
        return a == null ? b : a;
    }

    /**
     * <p>Removes the trailing slash from the input string, if it has one.</p>
     *
     * @param s the input string.
     * @return {@code null} if and only if the input string is {@code null}.
     */
    @CheckForNull public static String removeTrailingSlash(@Nullable String s) {
        if (s == null) return null;
        if (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    /**
     * <p>Deletes the given file and logs any exceptions that occur.</p>
     *
     * @param file no-op if {@code null}.
     */
    public static void deleteQuietly(@Nullable File file) {
        if (file == null) return;
        try {
            file.delete(); // NOSONAR
        } catch (SecurityException e) {
            // Be quiet.
        }
    }

    /**
     * <p>Writes a string to a file using the UTF-8 charset.</p>
     *
     * @param file the file to write. No-op if {@code null}.
     * @param contents the string to write to the file. No-op if {@code null} or empty.
     */
    public static void writeUtf8(@Nullable File file, @Nullable String contents) {
        if (file == null) return;
        if (isNullOrEmpty(contents)) return;
        try {
            write(file, contents.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Impossible UnsupportedEncodingException, please report this bug.", e);
        }
    }

    /**
     * <p>Writes a byte array to a file.</p>
     *
     * @param file the file to write. No-op if {@code null}.
     * @param bytes the bytes to write to the file. No-op if {@code null} or empty.
     */
    public static void write(@Nullable File file, @Nullable byte[] bytes) {
        if (file == null) return;
        if (bytes == null || bytes.length == 0) return;
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(bytes);
            outputStream.flush();
        } catch (IOException e) {
            LOGGER.warn("Could not write file.", e);
        }
    }

    /**
     * <p>Reads a file and returns its bytes.</p>
     *
     * @param file the file to read.
     * @return never {@code null}. If file is {@code null}, returned array is empty.
     * @throws IOException if an error occurs while reading the file.
     */
    @NonNull public static byte[] read(@Nullable File file) throws IOException {
        if (file == null) return new byte[0];
        if (file.length() > Integer.MAX_VALUE) throw new IOException("File too large: " + file.length());
        byte[] bytes = new byte[(int) file.length()];
        try (DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            stream.readFully(bytes);
        }
        return bytes;
    }

    /**
     * <p>Returns a string value, falling back on an environment variable or JVM system property.</p>
     *
     * @param fromThisObj this function returns this object if it is not {@code null}.
     * @param envVar if {@code fromThisObj} is {@code null} and the environment variable named by
     * this parameter is set, this function returns the value of that environment variable. If this
     * parameter is {@code null} or empty, no environment variable is checked.
     * @param sysProp if {@code fromThisObj} is {@code null}, and if {@code envVar} is {@code null},
     * empty, or refers to an environment variable that is not set, and the JVM system property named
     * by this parameter is set, this function returns the value of that JVM system property. If this
     * parameter is {@code null} or empty, no JVM system property is checked.
     * @return possibly {@code null}.
     */
    @CheckForNull public static String getValue(@Nullable String fromThisObj, @Nullable String envVar, @Nullable String sysProp) {
        if (fromThisObj != null) return fromThisObj;
        String fromEnv = null;
        if (!isNullOrEmpty(envVar)) {
            try {
                fromEnv = System.getenv(envVar);
            } catch (SecurityException e) {
                LOGGER.warn("Could not get env var '{}'.", envVar, e);
                // Skip this and try the JVM sysprop.
            }
        }
        if (fromEnv != null) return fromEnv;
        String fromProp = null;
        if (!isNullOrEmpty(sysProp)) {
            try {
                fromProp = System.getProperty(sysProp);
            } catch (SecurityException e) {
                LOGGER.warn("Could not get JVM sys prop '{}'.", sysProp, e);
            }
        }
        return fromProp;
    }

    /**
     * <p>Returns a boolean value, falling back on an environment variable or JVM system property.</p>
     *
     * @param fromThisObj this function returns this object if it is not {@code null}.
     * @param envVar if {@code fromThisObj} is {@code null} and the environment variable named by
     * this parameter is set, this function returns the value of that environment variable. If this
     * parameter is {@code null} or empty, no environment variable is checked.
     * @param sysProp if {@code fromThisObj} is {@code null}, and if {@code envVar} is {@code null},
     * empty, or refers to an environment variable that is not set, and the JVM system property named
     * by this parameter is set, this function returns the value of that JVM system property. If this
     * parameter is {@code null} or empty, no JVM system property is checked.
     * @return whatever boolean value is determined by the logic described above, or {@code false}
     * if all above described options fail to produce a value.
     */
    public static boolean getValue(@Nullable Boolean fromThisObj, @Nullable String envVar, @Nullable String sysProp) {
        if (fromThisObj != null) return fromThisObj;
        return Boolean.valueOf(getValue((String) null, envVar, sysProp));
    }

    /**
     * <p>Returns an integer value, falling back on an environment variable or JVM system property.</p>
     *
     * @param fromThisObj this function returns this object if it is not {@code null}.
     * @param envVar if {@code fromThisObj} is {@code null} and the environment variable named by
     * this parameter is set, this function returns the value of that environment variable. If this
     * parameter is {@code null} or empty, no environment variable is checked.
     * @param sysProp if {@code fromThisObj} is {@code null}, and if {@code envVar} is {@code null},
     * empty, or refers to an environment variable that is not set, and the JVM system property named
     * by this parameter is set, this function returns the value of that JVM system property. If this
     * parameter is {@code null} or empty, no JVM system property is checked.
     * @return whatever boolean value is determined by the logic described above, or {@code 0}
     * if all above described options fail to produce a value.
     */
    public static int getValue(@Nullable Integer fromThisObj, @Nullable String envVar, @Nullable String sysProp) {
        if (fromThisObj != null) return fromThisObj;
        String fromOther = getValue((String) null, envVar, sysProp);
        if (fromOther == null) return 0;
        try {
            return Integer.valueOf(fromOther);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * <p>Returns a thread factory that produces daemon threads.</p>
     *
     * @return never {@code null}.
     */
    @NonNull public static ThreadFactory createDaemonThreadFactory() {
        return new ThreadFactory() {
            @Override public Thread newThread(Runnable r) {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(true);
                return t;
            }
        };
    }

    /**
     * <p>Returns the MD5 hash of the MAC address of the first non-loopback network interface that is found by
     * this method.</p>
     *
     * @return never {@code null} or empty. If no such interface can be found, or if its MAC address cannot
     * be read, or if this JVM does not support the MD5 algorithm, the string literal {@code unknown} is
     * returned.
     */
    @NonNull public static String getMacAddressHash() {
        try {
            // Find the first non-loopback interface on the system with an accessible hardware (MAC) address.
            NetworkInterface networkInterface = null;
            Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
            while (n.hasMoreElements()) {
                NetworkInterface e = n.nextElement();
                if (!e.isLoopback() && e.getHardwareAddress() != null) {
                    networkInterface = e;
                    break;
                }
            }
            if (networkInterface == null) {
                LOGGER.warn("Could not find a non-loopback interface with an available MAC address.");
                return UNKNOWN_VALUE;
            }
            // Convert bytes of hardware address into human-readable form.
            byte[] mac = networkInterface.getHardwareAddress();
            StringBuilder humanReadable = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                humanReadable.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            // Hash the human-readable address using MD5.
            byte[] humanReadableBytes = humanReadable.toString().getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(humanReadableBytes);
            // Convert bytes of hashed hardware address into a hexadecimal string.
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < hashBytes.length; i++) {
                hexString.append(String.format("%x", hashBytes[i]));
            }
            return hexString.toString();
        } catch (SocketException | NoSuchAlgorithmException | UnsupportedEncodingException e) {
            LOGGER.warn("Error while calculating MAC address hash.", e);
            return UNKNOWN_VALUE;
        }
    }

    /**
     * <p>Gets the public IP address of the machine upon which this JVM is running, via
     * {@code http://checkip.amazonaws.com}.</p>
     *
     * @return never {@code null} or empty. If an error occurs, it is logged and this function returns
     * the string literal {@code unknown}.
     */
    @NonNull public static String getIpAddress() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new URL("https://checkip.amazonaws.com").openStream()))) {
            return defaultValue(in.readLine(), UNKNOWN_VALUE);
        } catch (IOException e) {
            LOGGER.warn("Could not get public IP address.", e);
            return UNKNOWN_VALUE;
        }
    }

}
