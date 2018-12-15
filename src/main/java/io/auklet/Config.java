package io.auklet;

import io.auklet.misc.Util;

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
 *       <td>{@code null} (Auklet will not write data to any serial port)</td>
 *     </tr>
 *   </tbody>
 * </table>
 *
 * <p>The <b>only</b> classes/methods in the Auklet agent Javadocs that are officially supported for end
 * users are:</p>
 *
 * <ul>
 *   <li>All {@code public static} methods in the {@link Auklet} class.</li>
 *   <li>The {@link Config} class.</li>
 *   <li>The {@link AukletException} class.</li>
 * </ul>
 *
 * <p><b>Unless instructed to do so by Auklet support, do not use any classes/fields/methods other than
 * those described above.</b></p>
 */
public final class Config {

    private String appId = null;
    private String apiKey = null;
    private String baseUrl = null;
    private String configDir = null;
    private Boolean autoShutdown = null;
    private Boolean uncaughtExceptionHandler = null;
    private String serialPort = null;

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

    /**
     * <p>Returns the desired app ID.</p>
     *
     * @return possibly {@code null}.
     */
    /*package*/ String getAppId() {
        return appId;
    }

    /**
     * <p>Returns the desired API key.</p>
     *
     * @return possibly {@code null}.
     */
    /*package*/ String getApiKey() {
        return apiKey;
    }

    /**
     * <p>Returns the desired API base URL.</p>
     *
     * @return possibly {@code null}.
     */
    /*package*/ String getBaseUrl() {
        return baseUrl;
    }

    /**
     * <p>Returns the desired config directory.</p>
     *
     * @return possibly {@code null}.
     */
    /*package*/ String getConfigDir() {
        return configDir;
    }

    /**
     * <p>Returns the desired auto-shutdown behavior.</p>
     *
     * @return possibly {@code null}.
     */
    /*package*/ Boolean getAutoShutdown() {
        return autoShutdown;
    }

    /**
     * <p>Returns the desired uncaught exception handler behavior.</p>
     *
     * @return possibly {@code null}.
     */
    /*package*/ Boolean getUncaughtExceptionHandler() {
        return uncaughtExceptionHandler;
    }

    /**
     * <p>Returns the desired serial port.</p>
     *
     * @return possibly {@code null}.
     */
    /*package*/ String getSerialPort() {
        return serialPort;
    }

}
