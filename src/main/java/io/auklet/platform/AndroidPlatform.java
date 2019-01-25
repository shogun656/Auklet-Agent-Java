package io.auklet.platform;

import android.content.Context;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.auklet.Auklet;
import io.auklet.AukletException;
import io.auklet.core.AndroidMetrics;
import org.msgpack.core.MessagePacker;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class AndroidPlatform implements Platform {

    private final Context context;
    private final AndroidMetrics metrics;

    public AndroidPlatform(Auklet agent, Context context) throws AukletException {
        this.context = context;
        this.metrics = new AndroidMetrics(context);
        this.metrics.start(agent);
    }

    @Override public List<String> getPossibleConfigDirs(@Nullable String fromConfig) {
        return Collections.singletonList(this.context.getFilesDir().getPath() + "/aukletFiles");
    }

    @Override public void addSystemMetrics(@NonNull MessagePacker msgpack) throws IOException {
        msgpack.packString("memoryUsage").packDouble(metrics.getMemoryUsage());
        msgpack.packString("cpuUsage").packDouble(metrics.getCPUUsage());
    }

    @Override public Boolean isAndroid() {
        return true;
    }
}
