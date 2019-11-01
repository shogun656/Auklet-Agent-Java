package io.auklet.platform;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.auklet.Auklet;
import io.auklet.AukletException;
import io.auklet.Config;
import io.auklet.config.DeviceAuth;
import io.auklet.core.HasAgent;
import io.auklet.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/** <p>Provides logic common to all platforms.</p> */
public abstract class AbstractPlatform extends HasAgent implements Platform {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPlatform.class);
    private static final String DIR_ERROR = "Skipping directory '{}' due to an error.";

    @Override public void start(@NonNull Auklet agent) throws AukletException {
        this.setAgent(agent);
    }

    @CheckForNull @Override public final File obtainConfigDir(@Nullable String fromConfig) {
        List<String> configDirs = getPossibleConfigDirs(fromConfig);
        LOGGER.debug("Checking directories for existing config files.");
        File existingConfigDir = chooseExistingConfigDir(configDirs);
        if (existingConfigDir != null) return existingConfigDir;
        LOGGER.debug("No existing config files found; checking directories for suitability.");
        return chooseNewConfigDir(configDirs);
    }

    /**
     * <p>Determines the possible config directories that can be used by the agent.</p>
     *
     * @param fromConfig the value from the {@link Config} object, env var and/or JVM sysprop, possibly
     * {@code null}.
     * @return the list of possible config directories that can be used on this platform.
     */
    @NonNull protected abstract List<String> getPossibleConfigDirs(@Nullable String fromConfig);

    /**
     * <p>Finds and returns the first existing config directory that contains an Auklet config file
     * that the JVM is able to read.</p>
     *
     * @param possibleConfigDirs the list of possible config directories that need to be tested; the
     * first (per iteration order) directory in this list that passes the tests will be returned.
     * @return possibly {@code null}, meaning that no possible config directories were suitable.
     */
    @CheckForNull
    private static File chooseExistingConfigDir(@Nullable List<String> possibleConfigDirs) {
        if (possibleConfigDirs != null) {
            for (String dir : possibleConfigDirs) {
                // If a directory contains the auth file, use that directory.
                // We don't care if the other files don't exist because we'll create them later if needed.
                File authFile = new File(dir, DeviceAuth.FILENAME);
                try {
                    if (authFile.exists()) {
                        LOGGER.debug("Using existing config directory: {}", dir);
                        return new File(dir);
                    }
                } catch (SecurityException e) {
                    handleSecurityException(e, dir);
                }
            }
        }
        return null;
    }

    /**
     * <p>Finds and returns the first eligible config directory to which the JVM is able to write files.</p>
     *
     * @param possibleConfigDirs the list of possible config directories that need to be tested; the
     * first (per iteration order) directory in this list that passes the tests will be returned.
     * @return possibly {@code null}, meaning that no possible config directories were suitable.
     */
    @CheckForNull
    private static File chooseNewConfigDir(@Nullable List<String> possibleConfigDirs) {
        if (possibleConfigDirs != null) {
            for (String dir : possibleConfigDirs) {
                try {
                    File dirFile = new File(dir);
                    if (dirTest(dirFile)) return dirFile;
                } catch (SecurityException e) {
                    handleSecurityException(e, dir);
                }
            }
        }
        return null;
    }

    /**
     * <p>Tests the given directory to see if it is creatable or writable.</p>
     *
     * @param dir the directory to test.
     * @return {@code true} if creatable or writable, in which case the directory is
     * guaranteed to exist upon return.
     * @throws IllegalArgumentException if the dir is {@code null}.
     */
    private static boolean dirTest(@NonNull File dir) {
        if (dir == null) throw new IllegalArgumentException("Dir is null");
        // Per Javadocs, File.mkdirs() no-ops with no exception if the given path already
        // exists *as a directory*. However, this result does not imply that the JVM has
        // write permissions *inside* the directory, which would be the case only if the
        // directory existed beforehand.
        //
        // To alleviate this, we do a test file write inside the directory *only if the
        // directory existed beforehand*.
        boolean alreadyExists = dir.exists();
        if (alreadyExists && dirTestWrite(dir)) {
            LOGGER.debug("Using existing config directory: {}", dir.getPath());
            return true;
        }
        if (!alreadyExists && dir.mkdirs()) {
            LOGGER.debug("Created new config directory: {}", dir.getPath());
            return true;
        }
        return false;
    }

    /**
     * <p>Tests whether a given directory is writable.</p>
     *
     * @param dir the directory to test.
     * @return {@code true} if it is writable.
     * @throws IllegalArgumentException if the dir is {@code null}.
     */
    private static boolean dirTestWrite(@NonNull File dir) {
        if (dir == null) throw new IllegalArgumentException("Dir is null");
        File tempFile = null;
        boolean success = true;
        try {
            tempFile = File.createTempFile("auklet", null, dir);
        } catch (IOException e) {
            LOGGER.warn(DIR_ERROR, dir, e);
            success = false;
        }
        FileUtil.deleteQuietly(tempFile);
        return success;
    }

    /**
     * <p>Logs a security exception while testing config dirs.</p>
     *
     * @param e the exception.
     * @param dir the directory being tested.
     */
    private static void handleSecurityException(@NonNull SecurityException e, @NonNull String dir) {
        if (e == null || dir == null) return;
        if (Auklet.LOUD_SECURITY_EXCEPTIONS) LOGGER.warn(DIR_ERROR, dir, e);
        else LOGGER.warn("Skipping directory '{}' due to an error: {}", dir, e.getMessage());
    }

}
