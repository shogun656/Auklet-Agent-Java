package io.auklet.misc;

import io.auklet.util.FileUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilTest {
    @Test
    void testDeleteQuietly() {
        String pathname = "./tmp/io.auklet.misc.UtilTest.testDeleteQuietly";
        File file = new File(pathname);
        FileUtil.deleteQuietly(file);
        assertFalse(file.isFile());
    }

    @Test void testWriteUtf8() throws IOException {
        String pathname = "./tmp/io.auklet.misc.UtilTest.testWriteUtf8";
        File file = new File(pathname);
        FileUtil.writeUtf8(file, "0");
        assertEquals("0", new String(Files.readAllBytes(Paths.get(pathname))));
    }

    @Test void testWrite() throws IOException {
        String pathname = "./tmp/io.auklet.misc.UtilTest.testWrite";
        String data = "0";
        byte[] bytes = data.getBytes("UTF-8");
        File file = new File(pathname);
        FileUtil.write(file, bytes);
        assertEquals("0", new String(Files.readAllBytes(Paths.get(pathname))));
    }

    @Test void testRead() throws IOException {
        String pathname = "./tmp/io.auklet.misc.UtilTest.testRead";
        String data = "0";
        byte[] bytes = data.getBytes("UTF-8");
        FileOutputStream writeFile = new FileOutputStream(pathname);
        File readFile = new File(pathname);
        writeFile.write(bytes);
        assertEquals("0", (new String(FileUtil.read(readFile), "UTF-8")));
    }
}
