package io.auklet.platform;

import android.content.Context;
import android.test.ServiceTestCase;
import io.auklet.AukletException;
import io.auklet.TestingTools;
import io.auklet.util.SysUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AndroidPlatformTest {
    private AndroidPlatform androidPlatform;
    private final MessageBufferPacker msgpack = MessagePack.newDefaultBufferPacker();

    @BeforeAll
    void setup() throws AukletException, IOException, URISyntaxException {
        androidPlatform = new AndroidPlatform(getTestContext());
        androidPlatform.start(aukletConstructor());
    }

    @Test
    void testGetPossibleConfigDirs() {
        List dirs = androidPlatform.getPossibleConfigDirs("");
        System.out.println(dirs.get(0));
    }

    @Test void testAddSystemMetrics() throws IOException, AukletException {
        assertTrue(new String(msgpack.toByteArray()).isEmpty());
        androidPlatform.addSystemMetrics(msgpack);

        String msgpackString = new String(msgpack.toByteArray());
        assertFalse(msgpackString.isEmpty());
        assertTrue(msgpackString.contains("memoryUsage"));
        assertTrue(msgpackString.contains("cpuUsage"));
    }

    private Context getTestContext()
    {
        try
        {
            Method getTestContext = ServiceTestCase.class.getMethod("getTestContext");
            return (Context) getTestContext.invoke(this);
        }
        catch (final Exception exception)
        {
            exception.printStackTrace();
            return null;
        }
    }
}
