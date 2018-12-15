package io.auklet.config;

import io.auklet.Auklet;
import mjson.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;

/**
 * <p>The <i>data usage tracker file</i> is used to persist between restarts the amount of data that has
 * been sent by the Auklet agent to the sink.</p>
 */
public class DataUsageTracker extends AbstractConfigFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataUsageTracker.class);
    private static final String USAGE_FILE = "usage";
    private static final String USAGE_KEY = "usage";

    private final Object lock = new Object();
    private long bytesSent = 0L;

    /**
     * <p>Constructor.</p>
     */
    public DataUsageTracker() {
        try {
            // If the file doesn't exist, create it.
            if (!this.file.exists()) {
                this.writeToDisk(0L);
            }
            // Read from disk.
            byte[] usageBytes = Files.readAllBytes(this.file.toPath());
            String usageString = new String(usageBytes, "UTF-8");
            // Parse the JSON and set relevant fields.
            Json usageJson = Json.make(usageString);
            this.bytesSent = usageJson.at(DataUsageTracker.USAGE_KEY).asLong();
        } catch (IOException | SecurityException | IllegalArgumentException e) {
            LOGGER.warn("Could not read data usage tracker file from disk, assuming zero usage", e);
        }
    }

    @Override
    public String getName() { return DataUsageTracker.USAGE_FILE; }

    /**
     * <p>Return the number of bytes sent so far.</p>
     *
     * @return the number of bytes sent.
     */
    public long getBytesSent() {
        synchronized (this.lock) {
            return this.bytesSent;
        }
    }

    /**
     * <p>Adds the input number of bytes to the current amount of bytes sent.</p>
     *
     * @param moreBytes no-op if less than 1.
     */
    public void addMoreData(long moreBytes) {
        if (moreBytes < 1) return;
        synchronized (this.lock) {
            this.bytesSent += moreBytes;
            this.saveUsage(this.bytesSent);
        }
    }

    /** <p>Resets the data usage to zero.</p> */
    public void reset() {
        synchronized (this.lock) {
            this.bytesSent = 0L;
            this.saveUsage(this.bytesSent);
        }
    }

    /**
     * <p>Asynchronously saves the current usage value to disk.</p>
     *
     * @param usage the current usage value.
     */
    private void saveUsage(long usage) {
        new Thread(() -> {
            try {
                this.writeToDisk(usage);
            } catch (IOException | SecurityException e) {
                LOGGER.warn("Could not save data usage to disk", e);
            }
        }).start();
    }

    /**
     * <p>Write the usage value to disk.</p>
     *
     * @param usage the current usage value.
     * @throws IOException if an error occurs while writing the file.
     * @throws SecurityException if an error occurs while writing the file.
     */
    private void writeToDisk(long usage) throws IOException {
        Json usageJson = Json.object();
        usageJson.set(DataUsageTracker.USAGE_KEY, usage);
        Files.write(this.file.toPath(), usageJson.toString().getBytes("UTF-8"));
    }

}
