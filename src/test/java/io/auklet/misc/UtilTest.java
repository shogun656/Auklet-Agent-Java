package io.auklet.misc;

import io.auklet.util.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class UtilTest {
    @Test void testIsNullOrEmpty() {
        assertEquals(true, Util.isNullOrEmpty(""));
        assertEquals(false, Util.isNullOrEmpty("1"));
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

    @Test void testDeleteQuietly() {
        String pathname = "./tmp/io.auklet.misc.UtilTest.testDeleteQuietly";
        File file = new File(pathname);
        FileUtil.deleteQuietly(file);
        assertEquals(false, file.isFile());
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

//    @Test void testGetValue() {
//        assertEquals("0", SysUtil.getValue("0", "1", "2"));
//        assertEquals(true, SysUtil.getValue(true, "1", "2"));
//        assertNull(SysUtil.getValue((String)null, "1", "2"));
//        assertNull(SysUtil.getValue((Boolean)null, "1", "2"));
//        assertNull(SysUtil.getValue((Integer)null, "1", "2"));
//    }

    @Test void testCreateDaemonThreadFactory() {
        assertNotNull(ThreadUtil.createDaemonThreadFactory("0"));
    }

    @Test void testReadJson() {
        assertNotEquals("{}", JsonUtil.readJson("{1:1}"));
    }

    @Test void testGetMacAddressHash() {
        assertNotNull(Util.getMacAddressHash());
    }

//    @Test void testGetIpAddress() {
//        assertNotNull(Util.getIpAddress());
//    }

    @Test void testShutdown() {
        ExecutorService es = new ExecutorService() {
            Boolean isShutdownBool = true;

            @Override
            public void shutdown() {
                isShutdownBool = false;
            }

            @Override
            public List<Runnable> shutdownNow() {
                return null;
            }

            @Override
            public boolean isShutdown() {
                return isShutdownBool;
            }

            @Override
            public boolean isTerminated() {
                return false;
            }

            @Override
            public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
                return false;
            }

            @Override
            public <T> Future<T> submit(Callable<T> task) {
                return null;
            }

            @Override
            public <T> Future<T> submit(Runnable task, T result) {
                return null;
            }

            @Override
            public Future<?> submit(Runnable task) {
                return null;
            }

            @Override
            public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
                return null;
            }

            @Override
            public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
                return null;
            }

            @Override
            public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
                return null;
            }

            @Override
            public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return null;
            }

            @Override
            public void execute(Runnable command) {

            }
        };
        ThreadUtil.shutdown(es);
        assertEquals(false, es.isShutdown());
    }
}