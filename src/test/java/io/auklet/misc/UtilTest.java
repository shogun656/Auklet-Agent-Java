package io.auklet.misc;

import io.auklet.util.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilTest {
    @Test void testIsNullOrEmpty() {
        assertTrue(Util.isNullOrEmpty(""));
        assertFalse(Util.isNullOrEmpty("1"));
    }

    @Test void testOrElse() {
        assertEquals("0", Util.orElse("0", "1"));
        assertEquals("1", Util.orElse(null, "1"));
    }

    @Test void testOrElseNullEmpty() {
        assertEquals("0", Util.orElseNullEmpty("0", "1"));
        assertEquals("1", Util.orElseNullEmpty(null, "1"));
    }

    @Test void testRemoveTrailingSlash() {
        assertNull(Util.removeTrailingSlash(null));
        assertEquals("1", Util.removeTrailingSlash("1/"));
    }

    @Test void testGetMacAddressHash() {
        assertNotNull(Util.getMacAddressHash());
    }
}