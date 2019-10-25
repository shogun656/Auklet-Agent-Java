package io.auklet.util;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** <p>Utility methods related to JVM system functionality.</p> */
public final class SysUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(SysUtil.class);

    private SysUtil() {}

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
    @CheckForNull
    public static String getValue(@Nullable String fromThisObj, @Nullable String envVar, @Nullable String sysProp) {
        if (fromThisObj != null) return fromThisObj;
        String fromEnv = null;
        try {
            if (!Util.isNullOrEmpty(envVar)) fromEnv = System.getenv(envVar);
        } catch (SecurityException e) {
            LOGGER.warn("Could not get env var '{}'.", envVar, e);
        }
        String fromProp = null;
        try {
            if (!Util.isNullOrEmpty(sysProp)) fromProp = System.getProperty(sysProp);
        } catch (SecurityException e) {
            LOGGER.warn("Could not get JVM sys prop '{}'.", sysProp, e);
        }
        return Util.orElse(fromEnv, fromProp);
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
     * @return whatever boolean value is determined by the logic described above, or {@code null}
     * if all above described options fail to produce a value.
     */
    @CheckForNull public static Boolean getValue(@Nullable Boolean fromThisObj, @Nullable String envVar, @Nullable String sysProp) {
        if (fromThisObj != null) return fromThisObj;
        String stringValue = getValue((String) null, envVar, sysProp);
        return stringValue == null ? null : Boolean.valueOf(stringValue);
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
     * @return whatever boolean value is determined by the logic described above, or {@code null}
     * if all above described options fail to produce a value.
     */
    @CheckForNull public static Integer getValue(@Nullable Integer fromThisObj, @Nullable String envVar, @Nullable String sysProp) {
        if (fromThisObj != null) return fromThisObj;
        String stringValue = getValue((String) null, envVar, sysProp);
        if (stringValue == null) return null;
        try {
            return Integer.valueOf(stringValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
