package io.auklet.core;

import io.auklet.misc.Util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TestUtil {
    @Test
    void testIsNullOrEmpty() {
        assertEquals(true, Util.isNullOrEmpty(""));
        assertEquals(false, Util.isNullOrEmpty("1"));
    }

    @Test
    void testOrElse() {
        assertEquals("0", Util.orElse("0", "1"));
        assertEquals("1", Util.orElse(null, "1"));
    }

    @Test
    void testOrElseNullEmpty() {
        assertEquals("0", Util.orElseNullEmpty("0", "1"));
        assertEquals("1", Util.orElseNullEmpty(null, "1"));
    }

    @Test
    void testRemoveTrailingSlash() {
        assertNull(Util.removeTrailingSlash(null));
        assertEquals("1", Util.removeTrailingSlash("1/"));
    }

    @Test
    void testDeleteQuietly() {
        File file = new File("/tmp/io.auklet.core.TestUtil.testDeleteQuietly");
        Util.deleteQuietly(file);
        assertEquals(false, file.isFile());
    }

    @Test
    void testWriteUtf8() throws IOException {
        String pathName = "/tmp/io.auklet.core.TestUtil.testWriteUtf8";
        File file = new File(pathName);
        Util.writeUtf8(file, "0");
        assertEquals("0", new String(Files.readAllBytes(Paths.get(pathName))));
    }
}