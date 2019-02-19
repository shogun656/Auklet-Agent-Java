package io.auklet.platform;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestJavaPlatform {
    private JavaPlatform javaPlatform;
    protected final MessageBufferPacker msgpack = MessagePack.newDefaultBufferPacker();

    @BeforeAll void setup() {
        javaPlatform = new JavaPlatform();
    }

    @Test void testGetPossibleConfigDirs() {
        assertNotNull(javaPlatform.getPossibleConfigDirs(null));
    }

    @Test void testAddSystemMetrics() throws IOException {
        javaPlatform.addSystemMetrics(msgpack);
        assertNotNull(msgpack.toByteArray());
    }
}
