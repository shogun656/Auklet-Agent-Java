//TODO: Fix test module
//package io.auklet.sink;
//
//import edu.umd.cs.findbugs.annotations.NonNull;
//import edu.umd.cs.findbugs.annotations.Nullable;
//import io.auklet.Auklet;
//import io.auklet.AukletException;
//import io.auklet.TestingTools;
//import io.auklet.config.DeviceAuth;
//import io.auklet.core.DataUsageMonitor;
//import io.auklet.core.HasAgent;
//import io.auklet.platform.AbstractPlatform;
//import io.auklet.platform.Platform;
//import mjson.Json;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.TestInstance;
//import org.junit.platform.commons.support.ReflectionSupport;
//import org.mockito.Mockito;
//import org.mockito.MockitoAnnotations;
//import org.msgpack.core.MessagePacker;
//
//import java.io.File;
//import java.io.IOException;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.util.List;
//
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//class AbstractSinkTest extends TestingTools {
//    private DeviceAuth deviceAuth;
//    private DataUsageMonitor usageMonitor;
//    private AbstractPlatform platform;
//    private AbstractSink abstractSink;
//
//    @BeforeAll void setup() throws AukletException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
//        final Auklet auklet = aukletConstructor();
//        deviceAuth = new DeviceAuth();
//        usageMonitor = new DataUsageMonitor();
//        platform = new AbstractPlatform() {
//            @NonNull
//            @Override
//            protected List<String> getPossibleConfigDirs(@Nullable String fromConfig) {
//                return null;
//            }
//
//            @Override
//            public void addSystemMetrics(@NonNull MessagePacker msgpack) throws IOException {
//
//            }
//        };
//        abstractSink = new AbstractSink() {
//            @Override
//            protected void write(@NonNull byte[] bytes) throws AukletException {
//
//            }
//
//            @Override
//            public void start(@NonNull Auklet agent) throws AukletException {
//                setAgent(agent);
//            }
//        };
//
//        writeToDisk(auklet, jsonConfig, DeviceAuth.FILENAME);
//
//        deviceAuth.start(auklet);
//        usageMonitor.start(auklet);
//        platform.start(auklet);
//        abstractSink.start(auklet);
//    }
//
//    @Test void testSend() throws AukletException {
//        abstractSink.send(new AukletException());
//    }
//
//}
