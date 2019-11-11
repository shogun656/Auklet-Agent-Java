package io.auklet.misc;

import io.auklet.util.SysUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SysUtilTest {
    @Test
    void testGetValue() {
        assertEquals("0", SysUtil.getValue("0", "1", "2", false));
        assertEquals(true, SysUtil.getValue(true, "1", "2", false));
        assertNull(SysUtil.getValue((String)null, "1", "2", false));
        assertNull(SysUtil.getValue((Boolean)null, "1", "2", false));
        assertNull(SysUtil.getValue((Integer)null, "1", "2", false));
    }
}
