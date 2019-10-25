package io.auklet.util;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Enumeration;

/** <p>Utility methods.</p> */
public final class Util {

    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);
    public static final String UTF_8 = "UTF-8";

    private Util() {}

    /**
     * <p>Returns whether or not a collection is either null or empty.</p>
     *
     * @param c the collection to check.
     * @return {@code c == null || c.isEmpty()}
     */
    public static boolean isNullOrEmpty(@Nullable Collection<?> c) {
        return c == null || c.isEmpty();
    }

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
    @CheckForNull public static String orElse(@Nullable String a, @Nullable String b) {
        return a == null ? b : a;
    }

    /**
     * <p>Returns the first argument if it is neither {@code null} nor empty, else returns the second argument.</p>
     *
     * @param a the first argument.
     * @param b the second argument.
     * @return {isNullOrEmpty(a) ? b : a}.
     */
    @CheckForNull public static String orElseNullEmpty(@Nullable String a, @Nullable String b) {
        return isNullOrEmpty(a) ? b : a;
    }

    /**
     * <p>Removes the trailing slash from the input string, if it has one.</p>
     *
     * @param s the input string.
     * @return {@code null} if and only if the input string is {@code null}.
     */
    @CheckForNull public static String removeTrailingSlash(@Nullable String s) {
        if (s == null) return null;
        if (s.endsWith("/")) s = s.substring(0, s.length() - 1);
        return s;
    }

    /**
     * <p>Closes the given closeable and silences any exceptions that occur.</p>
     *
     * @param c no-op if {@code null}.
     */
    public static void closeQuietly(@Nullable Closeable c) {
        if (c == null) return;
        try { c.close(); } // NOSONAR
        catch (IOException e) { /* Be quiet. */ }
    }

    /**
     * <p>Returns the MD5 hash of the MAC address of the first non-loopback network interface that is found by
     * this method.</p>
     *
     * @return never {@code null}. If no such interface can be found, or if its MAC address cannot be
     * read, or if this JVM does not support the MD5 algorithm, an empty string is returned.
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
                return "";
            }
            // Convert bytes of hardware address into human-readable form.
            byte[] mac = networkInterface.getHardwareAddress();
            StringBuilder humanReadable = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                humanReadable.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            // Hash the human-readable address using MD5.
            byte[] humanReadableBytes = humanReadable.toString().getBytes(UTF_8);
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
            return "";
        }
    }

}
