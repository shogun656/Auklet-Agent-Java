package io.auklet.platform;

import io.auklet.AukletException;
import io.auklet.TestingTools;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AndroidPlatformTest extends TestingTools {
    private AndroidPlatform androidPlatform;
    private final MessageBufferPacker msgpack = MessagePack.newDefaultBufferPacker();

    @BeforeAll void setup() throws AukletException, IOException, URISyntaxException {
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
}
