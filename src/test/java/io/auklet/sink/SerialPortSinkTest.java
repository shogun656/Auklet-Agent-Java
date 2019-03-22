package io.auklet.sink;

import io.auklet.Auklet;
import io.auklet.AukletException;
import io.auklet.Config;
import io.auklet.TestingTools;
import io.auklet.config.DeviceAuth;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import jssc.SerialPortList;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SerialPortSinkTest extends TestingTools {
    private SerialPortSink serialPortSink;
    private Auklet auklet;

    @BeforeAll void setup() throws AukletException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String[] portNames = SerialPortList.getPortNames();

        Config config = new Config()
                .setAppId("0123456789101112")
                .setApiKey("123")
                .setSerialPort("console"); // This is the only "serial port" that will return correctly cross-platform with nothing plugged in.
        auklet = aukletConstructor(config);

        DeviceAuth deviceAuth = new DeviceAuth();
        deviceAuth.start(auklet);

        serialPortSink = new SerialPortSink();
        serialPortSink.start(auklet);

    }

    @Test void testSend() throws AukletException {
        serialPortSink.send(null);
    }
}
