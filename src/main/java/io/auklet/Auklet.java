package io.auklet;

import io.auklet.daemon.DataUsageMonitor;
import io.auklet.jvm.AukletExceptionHandler;
import io.auklet.config.DataUsageLimit;
import io.auklet.config.DataUsageTracker;
import io.auklet.config.DeviceAuth;
import io.auklet.misc.Util;
import io.auklet.sink.AukletIoSink;
import io.auklet.sink.SerialPortSink;
import io.auklet.sink.Sink;
import io.auklet.sink.SinkException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

/**
 * <p>The entry point for the Auklet agent for Java and other JVM-compatible languages.</p>
 *
 * <p>This class is a singleton; explicit instantiation via reflection provides no advantages over the
 * {@link #init()}/{@link #init(Config)} methods and will throw an {@link IllegalStateException} if
 * reflection is used to attempt to construct a second instance of this class.</p>
 *
 * <p>The <b>only</b> classes/methods in the Auklet agent Javadocs that are officially supported for end
 * users are:</p>
 *
 * <ul>
 *   <li>All {@code public static} methods in the {@link Auklet} class.</li>
 *   <li>All {@code public} methods in the {@link Auklet.Config} class.</li>
 *   <li>The {@link AukletException} class.</li>
 * </ul>
 *
 * <p><b>Unless instructed to do so by Auklet support, do not use any classes/fields/methods other than
 * those described above.</b></p>
 *
 * <p>In addition to configuring the logging levels for individual classes in the Auklet agent, you can
 * configure HTTP request/response logging via the logger named {@code io.auklet.http}:</p>
 *
 * <ul>
 *     <li>Set the logger level to {@code TRACE} to log req/resp lines/headers/bodies.</li>
 *     <li>Set the logger level to {@code DEBUG} to log req/resp lines/headers.</li>
 *     <li>Set the logger level to {@code INFO} to log req/resp lines.</li>
 *     <li>Any other logger level disables HTTP logging.</li>
 *     <li>Due to a technical limitation, all messages logged to this logger will be logged at
 *     level {@code INFO}, taking into consideration the behavior of the levels described above.</li>
 * </ul>
 */
public final class Auklet {

    /* Static fields/methods/API, including the static initializer. */

    private static final Logger LOGGER = LoggerFactory.getLogger(Auklet.class);
    private static final Logger HTTP_LOGGER = LoggerFactory.getLogger("io.auklet.http");
    private static final Object AGENT_LOCK = new Object();
    private static String agentVersion = "unknown";
    private static Auklet agent = null;

    static {
        // Extract Auklet agent version from the manifest.
        try {
            InputStream manifestStream = Auklet.class.getClassLoader().getResourceAsStream("META-INF/MANIFEST.MF");
            if (manifestStream != null) {
                Manifest manifest = new Manifest(manifestStream);
                String version = manifest.getMainAttributes().getValue("Implementation-Version");
                Auklet.agentVersion = Util.defaultValue(version, "unknown");
            }
        } catch (SecurityException | IOException e) {
            LOGGER.warn("Could not obtain Auklet agent version from manifest", e);
        }
        // Initialize the Auklet agent if requested via env var or JVM sysprop.
        String fromEnv = System.getenv("AUKLET_AUTO_START");
        String fromProp = System.getProperty("auklet.auto.start");
        if (Boolean.valueOf(fromEnv) || Boolean.valueOf(fromProp)) {
            try {
                Auklet.init(null);
            } catch (AukletException e) {
                LOGGER.error("Could not auto-start Auklet agent", e);
            }
        }
    }

    /**
     * <p>Returns the version of the Auklet agent.</p>
     *
     * @return never {@code null}. May be the string literal {@code unknown}.
     */
    public static String getVersion() { return Auklet.agentVersion; }

    /**
     * <p>Initializes the agent with all configuration options specified via environment variables
     * and/or JVM system properties.</p>
     *
     * <p>If an exception is thrown by this method, all data submission methods are guaranteed to
     * silently no-op.</p>
     *
     * @throws AukletException if the agent cannot be initialized.
     * @throws IllegalArgumentException if the app ID and/or API key is either {@code null} or empty, or if
     * no config directory cannot be obtained.
     */
    public static void init() throws AukletException {
        Auklet.init(null);
    }

    /**
     * <p>Initializes the agent with the given configuration values, falling back on environment
     * variables, JVM system properties and/or default values where needed.</p>
     *
     * <p>If an exception is thrown by this method, all data submission methods are guaranteed to
     * silently no-op.</p>
     *
     * @param config the agent config object. Must not be @{code null}.
     * @throws AukletException if the agent cannot be initialized.
     */
    public static void init(Auklet.Config config) throws AukletException {
        synchronized (Auklet.AGENT_LOCK) {
            // We check this here to provide a proper message, in case the user accidentally attempted to
            // init twice. We check again in the constructor to prevent instantiation via reflection.
            if (Auklet.agent != null) throw new AukletException("Agent is already initialized; use Auklet.shutdown() first");
            // Construct the agent object, which finalizes the agent configuration.
            Auklet agent = new Auklet(config);
            // Get the device auth file.
            agent.deviceAuth = new DeviceAuth(agent);
            // Construct the child objects.
            if (agent.getSerialPort() != null) {
                agent.sink = new SerialPortSink(agent);
            } else {
                agent.sink = new AukletIoSink(agent);
            }
            // Start the data usage monitor.
            agent.usageLimit = new DataUsageLimit(agent);
            DataUsageTracker usageTracker = new DataUsageTracker(agent);
            agent.usageMonitor = new DataUsageMonitor(agent.usageLimit, usageTracker);
            // Define the JVM shutdown hook, if requested.
            if (agent.autoShutdown) {
                Thread shutdownHook = Auklet.createShutdownHook();
                agent.shutdownHook = shutdownHook;
                Runtime.getRuntime().addShutdownHook(shutdownHook);
            }
            // Enable the uncaught exception handler, if requested.
            if (agent.uncaughtExceptionHandler) {
                Thread.setDefaultUncaughtExceptionHandler(new AukletExceptionHandler());
            }
            // Set the global agent object.
            Auklet.agent = agent;
        }
    }

    /**
     * <p>Creates a JVM shutdown thread that shuts down the Auklet agent.</p>
     *
     * @return never {@code null}.
     */
    private static Thread createShutdownHook() {
        return new Thread(() -> {
            synchronized (Auklet.AGENT_LOCK) {
                if (Auklet.agent != null) {
                    try {
                        Auklet.agent.doShutdown(true);
                    } catch (Exception e) {
                        LOGGER.warn("Error while shutting down Auklet agent", e);
                    }
                }
            }
        });
    }

    /**
     * <p>Sends the given throwable to the agent as an <i>event</i>.</p>
     *
     * @param throwable if {@code null}, this method is no-op.
     * @throws AukletException if an error occurs while sending the event object to the Auklet data sink.
     */
    public static void send(Throwable throwable) throws AukletException {
        if (throwable == null) return;
        synchronized (Auklet.AGENT_LOCK) {
            if (Auklet.agent == null) return;
            Auklet.agent.sink.send(throwable);
        }
    }

    /**
     * <p>Shuts down the agent and closes/disconnects from any underlying resources. Calling this method more
     * than once has no effect; therefore, explicitly calling this method when the agent has been initialized
     * with a builtin JVM shutdown hook is unnecessary, unless you wish to shutdown the Auklet agent earlier
     * than JVM shutdown.</p>
     */
    public static void shutdown() {
        synchronized (Auklet.AGENT_LOCK) {
            if (Auklet.agent == null) return;
            Auklet.agent.doShutdown(false);
        }
    }

    /* Object-level fields/methods. */

    private final String appId;
    private final String apiKey;
    private final String baseUrl;
    private final File configDir;
    private final boolean autoShutdown;
    private final boolean uncaughtExceptionHandler;
    private final String serialPort;
    private final String macHash;
    private final String ipAddress;
    private final OkHttpClient http;
    private DeviceAuth deviceAuth;
    private Sink sink;
    private DataUsageLimit usageLimit;
    private DataUsageMonitor usageMonitor;
    private Thread shutdownHook;

    /**
     * <p>Auklet agent constructor, called via {@link #init(Config)}.</p>
     *
     * @param config possibly {@code null}.
     * @throws AukletException if the agent cannot be initialized.
     */
    private Auklet(Auklet.Config config) throws AukletException {
        synchronized (Auklet.AGENT_LOCK) {
            // We check this in the init method to provide a proper message, in case the user accidentally
            // attempted to init twice. We check again here to prevent instantiation via reflection.
            if (Auklet.agent != null) throw new AukletException("Use Auklet.init() to initialize the agent");
            // Validate/extract config.
            if (config == null) config = new Auklet.Config();
            this.appId = Util.getValue(config.appId, "AUKLET_APP_ID", "auklet.app.id");
            if (Util.isNullOrEmpty(this.appId)) throw new AukletException("App ID is null or empty");
            this.apiKey = Util.getValue(config.apiKey, "AUKLET_API_KEY", "auklet.api.key");
            if (Util.isNullOrEmpty(this.apiKey)) throw new AukletException("API key is null or empty");
            String baseUrl = Util.getValue(config.baseUrl, "AUKLET_BASE_URL", "auklet.base.url");
            this.baseUrl = Util.defaultValue(Util.removeTrailingSlash(baseUrl), "https://api.auklet.io");
            this.autoShutdown = Util.getValue(config.autoShutdown, "AUKLET_AUTO_SHUTDOWN", "auklet.auto.shutdown");
            this.uncaughtExceptionHandler = Util.getValue(config.uncaughtExceptionHandler, "AUKLET_UNCAUGHT_EXCEPTION_HANDLER", "auklet.uncaught.exception.handler");
            this.serialPort = Util.getValue(config.serialPort, "AUKLET_SERIAL_PORT", "auklet.serial.port");
            // Select/create/obtain the agent's config dir.
            this.configDir = this.obtainConfigDir(config.configDir);
            if (configDir == null) throw new AukletException("Could not find or create any config directory; see previous logged errors for details");
            // Setup other required internal fields.
            this.macHash = Util.getMacAddressHash();
            this.ipAddress = Util.getIpAddress();
            this.http = new OkHttpClient.Builder()
                    .addInterceptor(Auklet.createOkHttpLogger())
                    .build();

        }
    }

    /**
     * <p>Returns the app ID for this instance of the agent.</p>
     *
     * @return never {@code null}.
     */
    public String getAppId() {
        return this.appId;
    }

    /**
     * <p>Returns the API base URL for this instance of the agent.</p>
     *
     * @return never {@code null}.
     */
    public String getBaseUrl() {
        return this.baseUrl;
    }

    /**
     * <p>Returns the config directory for this instance of the agent.</p>
     *
     * @return never {@code null}.
     */
    public File getConfigDir() {
        return this.configDir;
    }

    /**
     * <p>Returns the serial port that will be used by this instance of the agent.</p>
     *
     * @return possibly {@code null}, in which case a serial port will not be used.
     */
    public String getSerialPort() {
        return this.serialPort;
    }

    /**
     * <p>Returns the MAC address hash for this instance of the agent.</p>
     *
     * @return never {@code null}.
     */
    public String getMacHash() {
        return this.macHash;
    }

    /**
     * <p>Returns the public IP address for this instance of the agent.</p>
     *
     * @return never {@code null}.
     */
    public String getIpAddress() {
        return this.ipAddress;
    }

    /**
     * <p>Returns the device auth for this instance of the agent.</p>
     *
     * @return never {@code null}.
     */
    public DeviceAuth getDeviceAuth() {
        return this.deviceAuth;
    }

    /**
     * <p>Returns the data usage limit for this instance of the agent.</p>
     *
     * @return never {@code null}.
     */
    public DataUsageLimit getUsageLimit() {
        return this.usageLimit;
    }

    /**
     * <p>Returns the data usage limit for this instance of the agent.</p>
     *
     * @return never {@code null}.
     */
    public DataUsageMonitor getUsageMonitor() {
        return this.usageMonitor;
    }

    /**
     * <p>Makes an authenticated request to the Auklet API.</p>
     *
     * @param request never {@code null}.
     * @return never {@code null}.
     * @throws AukletException if an error occurs with the request.
     */
    public Response api(Request.Builder request) throws AukletException {
        request.header("Authorization", "JWT " + this.apiKey);
        Request req = request.build();
        try {
            return this.http.newCall(req).execute();
        } catch (IOException e) {
            throw new AukletException("Error while making HTTP request", e);
        }
    }

    /**
     * <p>Returns the directory the Auklet agent will use to store its configuration files.</p>
     *
     * <p>Unlike other getter methods in this class, this method creates/tests write access to the
     * target config directory after determining which directory to use, per the logic described
     * in the class-level Javadoc.</p>
     *
     * @param fromConfigObject the value from the {@link Auklet.Config config object}, possibly
     * {@code null}.
     * @return possibly {@code null}, in which case the Auklet agent must throw an exception during
     * initialization and all data sent to the agent must be silently discarded.
     */
    private File obtainConfigDir(String fromConfigObject) {
        // Consider config dir settings in this order.
        List<String> possibleConfigDirs = Arrays.asList(
                fromConfigObject,
                System.getenv("AUKLET_CONFIG_DIR"),
                System.getProperty("auklet.config.dir"),
                System.getProperty("user.dir"),
                System.getProperty("user.home"),
                System.getProperty("java.io.tmpdir")
        );
        // Drop any env vars/sysprops whose value is null, and append the auklet subdir to each remaining value.
        possibleConfigDirs = possibleConfigDirs.stream()
                .filter(d -> d != null)
                .map(d -> d.replaceAll("/$", "") + "/aukletFiles")
                .collect(Collectors.toList());
        // If a directory contains the auth file, use that directory.
        // We don't care if the other files don't exist because we'll create them later if needed.
        for (String dir : possibleConfigDirs) {
            File authFile = new File(dir, DeviceAuth.FILENAME);
            try {
                if (authFile.exists()) {
                    LOGGER.info("Using existing config directory: {}", dir);
                    return new File(dir);
                }
            } catch (SecurityException e) {
                LOGGER.warn("Skipping directory {} due to an error", dir, e);
            }
        }
        // No existing config files were found. Use the first directory that we can create.
        for (String dir : possibleConfigDirs) {
            File configDir = new File(dir);
            try {
                Path configPath = configDir.toPath();
                boolean alreadyExists = Files.isDirectory(configPath);
                // Per Javadocs, Files.createDirectories() no-ops with no exception if the given
                // path already exists *as a directory*. However, this result does not imply
                // that the JVM has write permissions *inside* the directory, which would be the
                // case only if the directory existed prior to calling Files.createDirectories().
                //
                // To alleviate this, we do a test file write inside the directory *only if the
                // directory existed beforehand*.
                Files.createDirectories(configPath);
                if (alreadyExists) {
                    Path tempFile = Files.createTempFile(configPath, null, null);
                    LOGGER.info("Using existing config directory: {}", dir);
                    // Cleanup the temp file and warn if we can't. We handle these exceptions separately
                    // so that we don't consider this config dir unusable. This is a fairly pathological
                    // scenario and should rarely happen, if ever.
                    try {
                        Files.delete(tempFile);
                    } catch (IOException | SecurityException e) {
                        LOGGER.warn("Cannot clean up temp file in config dir {}; the Auklet agent should continue to work, but this may signify a filesystem permissions issue", dir, e);
                    }
                } else {
                    LOGGER.info("Created new config directory: {}", dir);
                }
                return configDir;
            } catch (IllegalArgumentException | UnsupportedOperationException | IOException | SecurityException e) {
                LOGGER.warn("Skipping directory {} due to an error", dir, e);
            }
        }
        return null;
    }

    /**
     * <p>Creates an OkHttp logging interceptor that sends all HTTP logs to the SLF4J logger named
     * {@code io.auklet.http}.</p>
     *
     * @return never {@code null}.
     */
    private static HttpLoggingInterceptor createOkHttpLogger() {
        HttpLoggingInterceptor.Level level;
        if (Auklet.HTTP_LOGGER.isTraceEnabled()) level = HttpLoggingInterceptor.Level.BODY;
        else if (Auklet.HTTP_LOGGER.isDebugEnabled()) level = HttpLoggingInterceptor.Level.HEADERS;
        else if (Auklet.HTTP_LOGGER.isInfoEnabled()) level = HttpLoggingInterceptor.Level.BASIC;
        else level = HttpLoggingInterceptor.Level.NONE;
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor((message) -> HTTP_LOGGER.info(message));
        logging.setLevel(level);
        return logging;
    }

    /**
     * <p>Shuts down the Auklet agent.</p>
     *
     * @param viaJvmHook {@code true} if shutdown is occurring due to a JVM hook, {@code false} otherwise.
     */
    private void doShutdown(boolean viaJvmHook) {
        LOGGER.info("Auklet agent is shutting down.");
        boolean jvmHookIsShuttingDown = this.shutdownHook != null && !viaJvmHook;
        // Remove the JVM shutdown hook if one is present, unless the hook is
        // the reason we're shutting down.
        if (!jvmHookIsShuttingDown) {
            Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
        }
        // Shutdown the sink, data usage monitor and API.
        try {
            this.sink.shutdown();
        } catch (SinkException e) {
            LOGGER.warn("Error while shutting down Auklet data sink", e);
        }
        this.usageMonitor.shutdown();
        try {
            this.http.dispatcher().executorService().shutdown();
            this.http.connectionPool().evictAll();
            this.http.cache().close();
        } catch (IOException e) {
            LOGGER.warn("Error while shutting down Auklet API", e);
        }
        // Drop the previous agent object reference, allowing the end user to re-initialize if desired.
        Auklet.agent = null;
    }

    /**
     * <p>Config object for the {@link Auklet} agent.</p>
     *
     * <p>The table below shows all the available configuration options, the order of preference when a
     * configuration option is set in multiple ways, and the default value for non-required options.</p>
     *
     * <table>
     *   <thead>
     *     <tr>
     *       <td>Config element</td>
     *       <td>Setter method</td>
     *       <td>Config value selection order</td>
     *       <td>Default value</td>
     *     </tr>
     *   </thead>
     *   <tbody>
     *     <tr>
     *       <td>Auto-start</td>
     *       <td><i>N/A</i></td>
     *       <td>
     *         <ol>
     *           <li>Environment variable {@code AUKLET_AUTO_START}</li>
     *           <li>JVM system property {@code auklet.auto.start}</li>
     *         </ol>
     *       </td>
     *       <td>{@code false} (auto-start is disabled)</td>
     *     </tr>
     *     <tr>
     *       <td>Application ID</td>
     *       <td>{@link #setAppId(String)}</td>
     *       <td>
     *         <ol>
     *           <li>Setter method value</li>
     *           <li>Environment variable {@code AUKLET_APP_ID}</li>
     *           <li>JVM system property {@code auklet.app.id}</li>
     *         </ol>
     *       </td>
     *       <td><i>N/A (required)</i></td>
     *     </tr>
     *     <tr>
     *       <td>API key</td>
     *       <td>{@link #setApiKey(String)}</td>
     *       <td>
     *         <ol>
     *           <li>Setter method value</li>
     *           <li>Environment variable {@code AUKLET_API_KEY}</li>
     *           <li>JVM system property {@code auklet.api.key}</li>
     *         </ol>
     *       </td>
     *       <td><i>N/A (required)</i></td>
     *     </tr>
     *     <tr>
     *       <td>Base URL</td>
     *       <td>{@link #setBaseUrl(String)}</td>
     *       <td>
     *         <ol>
     *           <li>Setter method value</li>
     *           <li>Environment variable {@code AUKLET_BASE_URL}</li>
     *           <li>JVM system property {@code auklet.base.url}</li>
     *         </ol>
     *       </td>
     *       <td>{@code https://api.auklet.io}</td>
     *     </tr>
     *     <tr>
     *       <td>Config directory</td>
     *       <td>{@link #setConfigDir(String)}</td>
     *       <td>
     *         <ol>
     *           <li>Setter method value</li>
     *           <li>Environment variable {@code AUKLET_CONFIG_DIR}</li>
     *           <li>JVM system property {@code auklet.config.dir}</li>
     *           <li>JVM system property {@code user.dir}</li>
     *           <li>JVM system property {@code user.home}</li>
     *           <li>JVM system property {@code java.io.tmpdir}</li>
     *         </ol>
     *       </td>
     *       <td><i>N/A (required)</i></td>
     *     </tr>
     *     <tr>
     *       <td>Auto-shutdown (via JVM shutdown hook)</td>
     *       <td>{@link #setAutoShutdown(Boolean)}</td>
     *       <td>
     *         <ol>
     *           <li>Setter method value</li>
     *           <li>Environment variable {@code AUKLET_AUTO_SHUTDOWN}</li>
     *           <li>JVM system property {@code auklet.auto.shutdown}</li>
     *         </ol>
     *       </td>
     *       <td>{@code false} (auto-shutdown is disabled)</td>
     *     </tr>
     *     <tr>
     *       <td>Default uncaught exception handler</td>
     *       <td>{@link #setUncaughtExceptionHandler(Boolean)}</td>
     *       <td>
     *         <ol>
     *           <li>Setter method value</li>
     *           <li>Environment variable {@code AUKLET_UNCAUGHT_EXCEPTION_HANDLER}</li>
     *           <li>JVM system property {@code auklet.uncaught.exception.handler}</li>
     *         </ol>
     *       </td>
     *       <td>{@code false} (uncaught exceptions will not be sent to Auklet)</td>
     *     </tr>
     *     <tr>
     *       <td>Serial port (instead of sending data to {@code auklet.io}</td>
     *       <td>{@link #setSerialPort(String)}</td>
     *       <td>
     *         <ol>
     *           <li>Setter method value</li>
     *           <li>Environment variable {@code AUKLET_SERIAL_PORT}</li>
     *           <li>JVM system property {@code auklet.serial.port}</li>
     *         </ol>
     *       </td>
     *       <td>{@code null} (Auklet will not send data to any serial port)</td>
     *     </tr>
     *   </tbody>
     * </table>
     *
     * <p>The <b>only</b> classes/methods in the Auklet agent Javadocs that are officially supported for end
     * users are:</p>
     *
     * <ul>
     *   <li>All {@code public static} methods in the {@link Auklet} class.</li>
     *   <li>All {@code public} methods in the {@link Auklet.Config} class.</li>
     *   <li>The {@link AukletException} class.</li>
     * </ul>
     *
     * <p><b>Unless instructed to do so by Auklet support, do not use any classes/fields/methods other than
     * those described above.</b></p>
     */
    public static final class Config {

        private String appId = null;
        private String apiKey = null;
        private String baseUrl = null;
        private String configDir = null;
        private Boolean autoShutdown = null;
        private Boolean uncaughtExceptionHandler = null;
        private String serialPort = null;

        public Config() {}

        /**
         * <p>Sets the Auklet agent's app ID.</p>
         *
         * @param appId possibly {@code null}.
         * @return {@code this}.
         */
        public Config setAppId(String appId) {
            if (Util.isNullOrEmpty(appId)) appId = null;
            this.appId = appId;
            return this;
        }

        /**
         * <p>Sets the Auklet agent's API key.</p>
         *
         * @param apiKey possibly {@code null}.
         * @return {@code this}.
         */
        public Config setApiKey(String apiKey) {
            if (Util.isNullOrEmpty(appId)) appId = null;
            this.apiKey = apiKey;
            return this;
        }

        /**
         * <p>Sets the base URL of the Auklet API.</p>
         *
         * @param baseUrl possibly {@code null}.
         * @return {@code this}.
         */
        public Config setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * <p>Sets the directory the Auklet agent will use to store its configuration files.</p>
         *
         * @param configDir possibly {@code null}.
         * @return {@code this}.
         */
        public Config setConfigDir(String configDir) {
            this.configDir = configDir;
            return this;
        }

        /**
         * <p>Tells the Auklet agent whether or not to setup a JVM shutdown hook to shut itself down.</p>
         *
         * @param autoShutdown possibly {@code null}.
         * @return {@code this}.
         */
        public Config setAutoShutdown(Boolean autoShutdown) {
            this.autoShutdown = autoShutdown;
            return this;
        }

        /**
         * <p>Tells the Auklet agent whether or not to setup a JVM-wide uncaught exception handler.</p>
         *
         * @param uncaughtExceptionHandler possibly {@code null}.
         * @return {@code this}.
         */
        public Config setUncaughtExceptionHandler(Boolean uncaughtExceptionHandler) {
            this.uncaughtExceptionHandler = uncaughtExceptionHandler;
            return this;
        }

        /**
         * <p>Tells the Auklet agent to write data to a serial port, instead of to {@code auklet.io}.</p>
         *
         * @param serialPort the name of the serial port to use; {@code null} disables writing to a serial port.
         * @return {@code this}.
         */
        public Config setSerialPort(String serialPort) {
            this.serialPort = serialPort;
            return this;
        }

    }

}
