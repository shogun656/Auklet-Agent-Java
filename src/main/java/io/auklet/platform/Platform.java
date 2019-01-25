package io.auklet.platform;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.auklet.Config;
import org.msgpack.core.MessagePacker;

import java.io.IOException;
import java.util.List;

public interface Platform {

    /**
     * <p>Determines the possible config directories Auklet can use based on the underlying platform.</p>
     *
     * @param fromConfig the value from the {@link Config} object, env var and/or JVM sysprop, possibly
     * {@code null}.
     * @return the list of possible config directories that we are able to use
     */
    @NonNull List<String> getPossibleConfigDirs(@Nullable String fromConfig);

    /**
     * <p>Adds JVM Memory and CPU Usage current position in the given MessagePacker as a map object.</p>
     *
     * @throws IllegalArgumentException if the MessagePacker is {@code null}.
     */
    void addSystemMetrics(@NonNull MessagePacker msgpack) throws IOException;

    /**
     * <p>Returns whether the current system is Android.</p>
     *
     * @return never {@code null}.
     */
    @NonNull Boolean isAndroid();
}
