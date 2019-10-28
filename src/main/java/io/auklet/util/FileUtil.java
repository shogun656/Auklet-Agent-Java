package io.auklet.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/** <p>Utility methods related to file manipulation.</p> */
public final class FileUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

    private FileUtil() {}

    /**
     * <p>Deletes the given file and silences any exceptions that occur.</p>
     *
     * @param file no-op if {@code null}.
     */
    public static void deleteQuietly(@Nullable File file) {
        if (file == null) return;
        try { file.delete(); } // NOSONAR
        catch (SecurityException e) { /* Be quiet. */ }
    }

    /**
     * <p>Writes a string to a file using the UTF-8 charset.</p>
     *
     * @param file the file to write. No-op if {@code null}.
     * @param contents the string to write to the file. No-op if {@code null} or empty.
     * @throws IOException if an I/O error occurs.
     */
    public static void writeUtf8(@Nullable File file, @Nullable String contents) throws IOException {
        if (file == null) return;
        if (Util.isNullOrEmpty(contents)) return;
        try {
            write(file, contents.getBytes(Util.UTF_8));
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Impossible UnsupportedEncodingException, please report this bug.", e);
        }
    }

    /**
     * <p>Writes a byte array to a file.</p>
     *
     * @param file the file to write. No-op if {@code null}.
     * @param bytes the bytes to write to the file. No-op if {@code null} or empty.
     * @throws IOException if an I/O error occurs.
     */
    public static void write(@Nullable File file, @Nullable byte[] bytes) throws IOException {
        if (file == null) return;
        if (bytes == null || bytes.length == 0) return;
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(bytes);
            outputStream.flush();
        } catch (SecurityException e) {
            throw new IOException(e);
        }
    }

    /**
     * <p>Reads a file and returns its bytes.</p>
     *
     * @param file the file to read.
     * @return never {@code null}. If file is {@code null} or does not exist, returned array is empty.
     * @throws IOException if an error occurs while reading the file.
     */
    @NonNull
    public static byte[] read(@Nullable File file) throws IOException {
        if (file == null || file.length() == 0) return new byte[0];
        if (file.length() > Integer.MAX_VALUE) throw new IOException("File too large: " + file.length());
        byte[] bytes = new byte[(int) file.length()];
        try (DataInputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            stream.readFully(bytes);
        }
        return bytes;
    }

}
