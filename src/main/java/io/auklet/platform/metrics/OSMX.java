package io.auklet.platform.metrics;

import edu.umd.cs.findbugs.annotations.NonNull;
import net.jcip.annotations.Immutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>A frontend for {@link java.lang.management.OperatingSystemMXBean},
 * {@link com.sun.management.OperatingSystemMXBean} and {@link com.sun.management.UnixOperatingSystemMXBean}, depending
 * on which OS is being used and whether or not the JVM implements the {@code com.sun} classes.</p>
 *
 * <p>Methods in this class that return strings could, if you were to use the bean object directly, throw
 * {@link SecurityException}; instead, this class hides those exceptions and returns empty strings if this
 * occurs.</p>
 *
 * <p>Methods in this class that return numeric values may return negative values under certain circumstances
 * (see original Javadocs), and will also return negative values if the underlying method is not supported
 * because the required {@code com.sun} class is missing.</p>
 *
 * <p>This is a singleton; use {@link #BEAN} to retrieve the instance. The {@link #BEAN} maintains no
 * state and is thus immutable.</p>
 */
@Immutable
public enum OSMX {

    BEAN;

    private static final Logger LOGGER = LoggerFactory.getLogger(OSMX.class);
    private static final java.lang.management.OperatingSystemMXBean realBean;
    private static final boolean IS_SUN;
    private static final boolean IS_SUN_UNIX;

    // Since enums do not serialize any instance fields, we use a static initializer
    // so that our (static) fields are initialized upon classload, prior to any
    // deserialization taking place.
    static {
        realBean = java.lang.management.ManagementFactory.getOperatingSystemMXBean();
        boolean sun = false;
        boolean sunUnix = false;
        try {
            sun = realBean instanceof com.sun.management.OperatingSystemMXBean; // NOSONAR
        } catch (NoClassDefFoundError e) {
            LOGGER.warn("com.sun.management.OperatingSystemMXBean does not exist; system/JVM memory and CPU stats will not be available.");
        }
        if (sun) {
            try {
                sunUnix = realBean instanceof com.sun.management.UnixOperatingSystemMXBean; // NOSONAR
                LOGGER.info("Running on Unix platform.");
            } catch (NoClassDefFoundError e) {
                LOGGER.info("Running on non-Unix platform.");
            }
        }
        IS_SUN = sun;
        IS_SUN_UNIX = sunUnix;
    }

    @NonNull public String getName() {
        try {
            return realBean.getName();
        } catch (SecurityException e) {
            LOGGER.warn("Cannot get OS name", e);
            return "";
        }
    }
    @NonNull public String getArch() {
        try {
            return realBean.getArch();
        } catch (SecurityException e) {
            LOGGER.warn("Cannot get OS arch", e);
            return "";
        }
    }
    @NonNull public String getVersion() {
        try {
            return realBean.getVersion();
        } catch (SecurityException e) {
            LOGGER.warn("Cannot get OS version", e);
            return "";
        }
    }
    public int getAvailableProcessors() {
        return realBean.getAvailableProcessors();
    }
    public double getSystemLoadAverage() { return realBean.getSystemLoadAverage(); }
    public long getCommittedVirtualMemorySize() {
        long value = -1;
        if (IS_SUN) value = asSun().getCommittedVirtualMemorySize();
        return value;
    }
    public long getTotalSwapSpaceSize() {
        long value = -1;
        if (IS_SUN) value = asSun().getTotalSwapSpaceSize();
        return value;
    }
    public long getFreeSwapSpaceSize() {
        long value = -1;
        if (IS_SUN) value = asSun().getFreeSwapSpaceSize();
        return value;
    }
    public long getProcessCpuTime() {
        long value = -1;
        if (IS_SUN) value = asSun().getProcessCpuTime();
        return value;
    }
    public long getFreePhysicalMemorySize() {
        long value = -1;
        if (IS_SUN) value = asSun().getFreePhysicalMemorySize();
        return value;
    }
    public long getTotalPhysicalMemorySize() {
        long value = -1;
        if (IS_SUN) value = asSun().getTotalPhysicalMemorySize();
        return value;
    }
    public double getSystemCpuLoad() {
        double value = -1;
        if (IS_SUN) value = asSun().getSystemCpuLoad();
        return value;
    }
    public double getProcessCpuLoad() {
        double value = -1;
        if (IS_SUN) value = asSun().getProcessCpuLoad();
        return value;
    }
    public long getOpenFileDescriptorCount() {
        long value = -1;
        if (IS_SUN_UNIX) value = asSunUnix().getOpenFileDescriptorCount();
        return value;
    }
    public long getMaxFileDescriptorCount() {
        long value = -1;
        if (IS_SUN_UNIX) value = asSunUnix().getMaxFileDescriptorCount();
        return value;
    }

    // These methods return the realBean object cast to the appropriate type.
    // Calls to these methods must always be guarded by checking the corresponding
    // boolean flag via an if/else statement. Do not use ternary operators for this,
    // as it will cause NoClassDefFoundErrors on JVMs that lack com.sun packages.
    @NonNull private static com.sun.management.OperatingSystemMXBean asSun() {
        return (com.sun.management.OperatingSystemMXBean) realBean;
    }
    @NonNull private static com.sun.management.UnixOperatingSystemMXBean asSunUnix() {
        return (com.sun.management.UnixOperatingSystemMXBean) realBean;
    }

}
