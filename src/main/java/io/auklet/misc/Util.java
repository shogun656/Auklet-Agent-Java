package io.auklet.misc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

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
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    /**
     * <p>Returns the first argument if it is not {@code null}, else returns the second argument.</p>
     *
     * @param a the first argument.
     * @param b the second argument.
     * @return {@code a == null ? b : a}.
     */
    public static String defaultValue(String a, String b) {
        return a == null ? b : a;
    }

    /**
     * <p>Removes the trailing slash from the input string, if it has one.</p>
     *
     * @param s the input string.
     * @return {@code null} if and only if the input string is {@code null}.
     */
    public static String removeTrailingSlash(String s) {
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
    public static void deleteQuietly(Path file) {
        if (file == null) return;
        try {
            Files.delete(file);
        } catch (IOException | SecurityException e) {
            LOGGER.warn("Cannot delete file {}", file.toFile().getAbsolutePath(), e);
        }
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
    public static String getValue(String fromThisObj, String envVar, String sysProp) {
        if (fromThisObj != null) return fromThisObj;
        String fromEnv = null;
        if (!Util.isNullOrEmpty(envVar)) {
            try {
                fromEnv = System.getenv(envVar);
            } catch (SecurityException e) {
                LOGGER.warn("Could not check env var {}", envVar, e);
                // Skip this and try the JVM sysprop.
            }
        }
        if (fromEnv != null) return fromEnv;
        String fromProp = null;
        if (!Util.isNullOrEmpty(sysProp)) {
            try {
                fromProp = System.getProperty(sysProp);
            } catch (SecurityException e) {
                LOGGER.warn("Could not check JVM sys prop {}", sysProp, e);
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
    public static boolean getValue(Boolean fromThisObj, String envVar, String sysProp) {
        if (fromThisObj != null) return fromThisObj;
        return Boolean.valueOf(Util.getValue((String) null, envVar, sysProp));
    }

    /**
     * <p>Returns the MD5 hash of the MAC address of the first non-loopback network interface that is found by
     * this method.</p>
     *
     * @return never {@code null}. If no such interface can be found, or if its MAC address cannot be read,
     * or if this JVM does not support the MD5 algorithm, the string literal {@code unknown} is returned.
     */
    public static String getMacAddressHash() {
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
                return Util.UNKNOWN_VALUE;
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
            LOGGER.warn("Error while calculating MAC address hash", e);
            return Util.UNKNOWN_VALUE;
        }
    }

    /**
     * <p>Gets the public IP address of the machine upon which this JVM is running, via
     * {@code http://checkip.amazonaws.com}.</p>
     *
     * @return never {@code null}. If an error occurs, it is logged and this function returns the string
     * literal {@code unknown}.
     */
    public static String getIpAddress() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new URL("http://checkip.amazonaws.com").openStream()))) {
            return Util.defaultValue(in.readLine(), Util.UNKNOWN_VALUE);
        } catch (IOException e) {
            LOGGER.warn("Could not get public IP address", e);
            return Util.UNKNOWN_VALUE;
        }
    }

}
