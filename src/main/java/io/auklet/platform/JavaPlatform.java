package io.auklet.platform;

import com.github.dmstocking.optional.java.util.Optional;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.auklet.misc.Util;
import io.auklet.metrics.OSMX;
import org.msgpack.core.MessagePacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>This class contain Java specific helper functions.</p>
 */
public class JavaPlatform extends AbstractPlatform {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaPlatform.class);

    @Override public List<String> getPossibleConfigDirs(@Nullable String fromConfig) {
        if (Util.isNullOrEmpty(fromConfig)) LOGGER.warn("Config dir not defined, will attempt to fallback on JVM system properties.");

        // Consider config dir settings in this order.
        List<String> possibleConfigDirs = Arrays.asList(
                fromConfig,
                System.getProperty("user.dir"),
                System.getProperty("user.home"),
                System.getProperty("java.io.tmpdir")
        );

        // Drop any env vars/sysprops whose value is null, and append the auklet subdir to each remaining value.
        List<String> filteredConfigDirs = new ArrayList<>();
        for (String dir : possibleConfigDirs) {
            if (!Util.isNullOrEmpty(dir)) filteredConfigDirs.add(Util.removeTrailingSlash(dir) + "/aukletFiles");
        }
        return filteredConfigDirs;
    }

    @Override public void addSystemMetrics(@NonNull MessagePacker msgpack) throws IOException {
        // Calculate memory usage.
        double memUsage;
        Optional<Long> freeMem = OSMX.BEAN.getFreePhysicalMemorySize();
        Optional<Long> totalMem = OSMX.BEAN.getTotalPhysicalMemorySize();
        if (freeMem.isPresent() && totalMem.isPresent()) {
            memUsage = 100 * (1 - ((double) freeMem.get() / (double) totalMem.get()));
        } else {
            memUsage = 0d;
        }
        msgpack.packString("memoryUsage").packDouble(memUsage);
        // Calculate CPU usage.
        double cpuUsage;
        Optional<Double> loadAvg = OSMX.BEAN.getSystemLoadAverage();
        if (loadAvg.isPresent()) {
            cpuUsage = 100 * (loadAvg.get() / OSMX.BEAN.getAvailableProcessors());
        } else {
            cpuUsage = 0d;
        }
        msgpack.packString("cpuUsage").packDouble(cpuUsage);
    }

}
