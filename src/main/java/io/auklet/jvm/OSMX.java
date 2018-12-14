package io.auklet.jvm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.util.Optional;

/**
 * <p>A frontend for {@link java.lang.management.OperatingSystemMXBean},
 * {@link com.sun.management.OperatingSystemMXBean} and {@link com.sun.management.UnixOperatingSystemMXBean}, depending
 * on which OS is being used and whether or not the JVM implements the {@code com.sun} classes.</p>
 *
 * <p>Most methods in this class return {@link Optional}, and will return {@link Optional#empty()} when the underlying
 * MXBean returns a value that is considered exceptional (e.g. when
 * {@link java.lang.management.OperatingSystemMXBean#getSystemLoadAverage()} returns a negative value). Check the
 * Javadocs for the above classes for details on exceptional return values. Methods that would otherwise throw a
 * {@link SecurityException} will instead return {@link Optional#empty()}.</p>
 *
 * <p>This is a singleton; use {@link #BEAN} to retrieve the instance.</p>
 */
public enum OSMX {

    BEAN;

    private static final Logger LOGGER = LoggerFactory.getLogger(OSMX.class);
    private static java.lang.management.OperatingSystemMXBean realBean;
    private static boolean isSun;
    private static boolean isSunUnix;

    // Since enums do not serialize any instance fields, we use a static initializer
    // so that our (static) fields are initialized upon classload, prior to any
    // deserialization taking place.
    static {
        OSMX.realBean = ManagementFactory.getOperatingSystemMXBean();
        boolean sun = false;
        boolean sunUnix = false;
        try {
            sun = OSMX.realBean instanceof com.sun.management.OperatingSystemMXBean; // NOSONAR
        } catch (NoClassDefFoundError e) {
            LOGGER.warn("com.sun.management.OperatingSystemMXBean does not exist; system memory and JVM CPU usage stats will be available.");
        }
        if (sun) {
            try {
                sunUnix = OSMX.realBean instanceof com.sun.management.UnixOperatingSystemMXBean; // NOSONAR
            } catch (NoClassDefFoundError e) {
                // No need to log; presumably the end user knows if they're running on Unix or not.
            }
        }
        OSMX.isSun = sun;
        OSMX.isSunUnix = sunUnix;
    }

    public Optional<String> getName() {
        try {
            return Optional.ofNullable(OSMX.realBean.getName());
        } catch (SecurityException e) {
            LOGGER.warn("Cannot get OS name", e);
            return Optional.empty();
        }
    }
    public Optional<String> getArch() {
        try {
            return Optional.ofNullable(OSMX.realBean.getArch());
        } catch (SecurityException e) {
            LOGGER.warn("Cannot get OS arch", e);
            return Optional.empty();
        }
    }
    public Optional<String> getVersion() {
        try {
            return Optional.ofNullable(OSMX.realBean.getVersion());
        } catch (SecurityException e) {
            LOGGER.warn("Cannot get OS version", e);
            return Optional.empty();
        }
    }
    public int getAvailableProcessors() {
        return OSMX.realBean.getAvailableProcessors();
    }
    public Optional<Double> getSystemLoadAverage() {
        double value = OSMX.realBean.getSystemLoadAverage();
        return value < 0 ? Optional.empty() : Optional.of(value);
    }
    public Optional<Long> getCommittedVirtualMemorySize() {
        if (OSMX.isSun) {
            long value = OSMX.asSun().getCommittedVirtualMemorySize();
            return value == -1 ? Optional.empty() : Optional.of(value);
        }
        else return Optional.empty();
    }
    public Optional<Long> getTotalSwapSpaceSize() {
        if (OSMX.isSun) return Optional.of(OSMX.asSun().getTotalSwapSpaceSize());
        else return Optional.empty();
    }
    public Optional<Long> getFreeSwapSpaceSize() {
        if (OSMX.isSun) return Optional.of(OSMX.asSun().getFreeSwapSpaceSize());
        else return Optional.empty();
    }
    public Optional<Long> getProcessCpuTime() {
        if (OSMX.isSun) {
            long value = OSMX.asSun().getProcessCpuTime();
            return value == -1 ? Optional.empty() : Optional.of(value);
        }
        else return Optional.empty();
    }
    public Optional<Long> getFreePhysicalMemorySize() {
        if (OSMX.isSun) return Optional.of(OSMX.asSun().getFreePhysicalMemorySize());
        else return Optional.empty();
    }
    public Optional<Long> getTotalPhysicalMemorySize() {
        if (OSMX.isSun) return Optional.of(OSMX.asSun().getTotalPhysicalMemorySize());
        else return Optional.empty();
    }
    public Optional<Double> getSystemCpuLoad() {
        if (OSMX.isSun) {
            double value = OSMX.asSun().getSystemCpuLoad();
            return value < 0 ? Optional.empty() : Optional.of(value);
        }
        else return Optional.empty();
    }
    public Optional<Double> getProcessCpuLoad() {
        if (OSMX.isSun) {
            double value = OSMX.asSun().getProcessCpuLoad();
            return value < 0 ? Optional.empty() : Optional.of(value);
        }
        else return Optional.empty();
    }
    public Optional<Long> getOpenFileDescriptorCount() {
        if (OSMX.isSunUnix) return Optional.of(OSMX.asSunUnix().getOpenFileDescriptorCount());
        else return Optional.empty();
    }
    public Optional<Long> getMaxFileDescriptorCount() {
        if (OSMX.isSunUnix) return Optional.of(OSMX.asSunUnix().getMaxFileDescriptorCount());
        else return Optional.empty();
    }

    // These methods return the realBean object cast to the appropriate type.
    // Calls to these methods must always be guarded by checking the corresponding
    // boolean flag via an if/else statement. Do not use ternary operators for this,
    // as it will cause NoClassDefFoundErrors on JVMs that lack com.sun packages.
    private static com.sun.management.OperatingSystemMXBean asSun() {
        return (com.sun.management.OperatingSystemMXBean) OSMX.realBean;
    }
    private static com.sun.management.UnixOperatingSystemMXBean asSunUnix() {
        return (com.sun.management.UnixOperatingSystemMXBean) OSMX.realBean;
    }

}
