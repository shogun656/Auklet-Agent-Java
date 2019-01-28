package io.auklet.platform;

import android.content.Context;
import android.os.Build;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.auklet.Auklet;
import io.auklet.AukletException;
import io.auklet.Config;
import io.auklet.metrics.AndroidMetrics;
import org.msgpack.core.MessagePacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>This class contain Android specific helper functions.</p>
 */
public class AndroidPlatform extends AbstractPlatform {

    private final Context context;
    private final AndroidMetrics metrics;

    public AndroidPlatform(Context context) throws AukletException {
        this.context = context;
        this.metrics = new AndroidMetrics(context);
    }

    @Override public void start(@NonNull Auklet agent) throws AukletException {
        super.start(agent);

        Runnable cpuUsage = metrics.calculateCpuUsage();
        if(cpuUsage != null) agent.scheduleRepeatingTask(cpuUsage, 0L, 1L, TimeUnit.SECONDS);
    }

    /**
     * <p>Determines the possible config directories Auklet can use based on the underlying platform.</p>
     *
     * @param fromConfig the value from the {@link Config} object, env var and/or JVM sysprop, possibly
     * {@code null}. Unused in this implementation
     * @return the list of possible config directories that we are able to use. This method will return
     * exactly 1 element
     */
    @Override public List<String> getPossibleConfigDirs(@Nullable String fromConfig) {
        return Collections.singletonList(this.context.getFilesDir().getPath() + "/aukletFiles");
    }

    @Override public void addSystemMetrics(@NonNull MessagePacker msgpack) throws IOException {
        msgpack.packString("memoryUsage").packDouble(metrics.getMemoryUsage());
        msgpack.packString("cpuUsage").packDouble(metrics.getCpuUsage());
    }

}
