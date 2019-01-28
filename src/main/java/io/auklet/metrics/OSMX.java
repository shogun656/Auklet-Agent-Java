package io.auklet.metrics;

import edu.umd.cs.findbugs.annotations.NonNull;
import net.jcip.annotations.Immutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dmstocking.optional.java.util.Optional;

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

    @NonNull public Optional<String> getName() {
        try {
            return Optional.ofNullable(realBean.getName());
        } catch (SecurityException e) {
            LOGGER.warn("Cannot get OS name", e);
            return Optional.empty();
        }
    }
    @NonNull public Optional<String> getArch() {
        try {
            return Optional.ofNullable(realBean.getArch());
        } catch (SecurityException e) {
            LOGGER.warn("Cannot get OS arch", e);
            return Optional.empty();
        }
    }
    @NonNull public Optional<String> getVersion() {
        try {
            return Optional.ofNullable(realBean.getVersion());
        } catch (SecurityException e) {
            LOGGER.warn("Cannot get OS version", e);
            return Optional.empty();
        }
    }
    public int getAvailableProcessors() {
        return realBean.getAvailableProcessors();
    }
    @NonNull public Optional<Double> getSystemLoadAverage() {
        return convertDoubleLessThanZero(realBean.getSystemLoadAverage());
    }
    @NonNull public Optional<Long> getCommittedVirtualMemorySize() {
        long value = -1;
        if (IS_SUN) value = asSun().getCommittedVirtualMemorySize();
        return convertLongNegativeOne(value);
    }
    @NonNull public Optional<Long> getTotalSwapSpaceSize() {
        if (IS_SUN) return Optional.of(asSun().getTotalSwapSpaceSize());
        else return Optional.empty();
    }
    @NonNull public Optional<Long> getFreeSwapSpaceSize() {
        if (IS_SUN) return Optional.of(asSun().getFreeSwapSpaceSize());
        else return Optional.empty();
    }
    @NonNull public Optional<Long> getProcessCpuTime() {
        long value = -1;
        if (IS_SUN) value = asSun().getProcessCpuTime();
        return convertLongNegativeOne(value);
    }
    @NonNull public Optional<Long> getFreePhysicalMemorySize() {
        if (IS_SUN) return Optional.of(asSun().getFreePhysicalMemorySize());
        else return Optional.empty();
    }
    @NonNull public Optional<Long> getTotalPhysicalMemorySize() {
        if (IS_SUN) return Optional.of(asSun().getTotalPhysicalMemorySize());
        else return Optional.empty();
    }
    @NonNull public Optional<Double> getSystemCpuLoad() {
        double value = 0;
        if (IS_SUN) value = asSun().getSystemCpuLoad();
        return convertDoubleLessThanZero(value);
    }
    @NonNull public Optional<Double> getProcessCpuLoad() {
        double value = 0;
        if (IS_SUN) value = asSun().getProcessCpuLoad();
        return convertDoubleLessThanZero(value);
    }
    @NonNull public Optional<Long> getOpenFileDescriptorCount() {
        if (IS_SUN_UNIX) return Optional.of(asSunUnix().getOpenFileDescriptorCount());
        else return Optional.empty();
    }
    @NonNull public Optional<Long> getMaxFileDescriptorCount() {
        if (IS_SUN_UNIX) return Optional.of(asSunUnix().getMaxFileDescriptorCount());
        else return Optional.empty();
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

    // These methods convert return values into optional equivalents.
    @NonNull private static Optional<Long> convertLongNegativeOne(long value) {
        if (value == -1) return Optional.empty();
        else return Optional.of(value);
    }
    @NonNull private static Optional<Double> convertDoubleLessThanZero(double value) {
        if (value < 0) return Optional.empty();
        else return Optional.of(value);
    }

}
