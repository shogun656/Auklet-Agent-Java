package io.auklet.misc;

import io.auklet.util.JsonUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

class JsonUtilTest {
    @Test
    void testReadJson() {
        assertNotEquals("{}", JsonUtil.readJson("{1:1}"));
    }
}
