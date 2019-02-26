package io.auklet;

import io.auklet.config.DeviceAuth;
import io.auklet.core.AukletApi;
import io.auklet.core.DataUsageMonitor;
import io.auklet.platform.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.TestInstance;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestAuklet {
    private Auklet auklet;
    private TestLogger logger = TestLoggerFactory.getTestLogger(Auklet.class);

    @BeforeAll void setup() throws AukletException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, RuntimeException {
        Config config = new Config().setAppId("0123456789101112")
                .setApiKey("123");

        Constructor<Auklet> aukletConstructor = Auklet.class.getDeclaredConstructor(config.getClass());
        aukletConstructor.setAccessible(true);
        auklet = aukletConstructor.newInstance(config);

        Auklet.init(config);
    }

    @Test void testSend() {
        auklet.send(null);
        String loggingResult = logger.getLoggingEvents().asList().toString();
        assertEquals(true, loggingResult.contains("Ignoring send request for null throwable."));
        TestLoggerFactory.clear();

        try {
            throw new AukletException();
        } catch (AukletException e) {
            Auklet.send(e);
        }
    }

    @Test void testGetAppId() {
        assertEquals("0123456789101112", auklet.getAppId());
    }

    @Test void testGetBaseUrl() {
        assertEquals("https://api.auklet.io", auklet.getBaseUrl());
    }

    @Test void testGetConfigDir() {
        assertEquals(new File("aukletFiles").getAbsoluteFile(), auklet.getConfigDir());
    }

    @Test void testGetSerialPort() {
        assertNull(auklet.getSerialPort());
    }

    @Test void testGetMqttThreads() {
        assertEquals(true, auklet.getMqttThreads() > 1);
    }

    @Test void testGetMacHash() {
        assertNotNull(auklet.getMacHash());
    }

    @Test void testGetIpAddress() {
        assertNotNull(auklet.getIpAddress());
    }

    @Test void testGetApi() {
        assertThat(auklet.getApi(), instanceOf(AukletApi.class));
    }

    @Test void testGetDeviceAuth() {
        assertThat(auklet.getDeviceAuth(), instanceOf(DeviceAuth.class));
    }

    @Test void testGetUsageMonitor() {
        assertThat(auklet.getUsageMonitor(), instanceOf(DataUsageMonitor.class));
    }

    @Test void testGetPlatform() {
        assertThat(auklet.getPlatform(), instanceOf(Platform.class));
    }

    @Test void testScheduleOneShotTask() throws AukletException {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

            }
        };
        System.out.println(auklet.scheduleOneShotTask(runnable, 1, TimeUnit.SECONDS));
    }

    @Test void testScheduleRepeatingTask() throws AukletException {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

            }
        };
        System.out.println(auklet.scheduleRepeatingTask(runnable, 1,1, TimeUnit.SECONDS));
    }
}