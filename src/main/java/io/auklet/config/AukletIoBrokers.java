package io.auklet.config;

import io.auklet.Auklet;
import io.auklet.AukletException;
import mjson.Json;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * <p>This file contains MQTT connection information for sending data to {@code auklet.io}.</p>
 */
public final class AukletIoBrokers extends AbstractConfigFileFromApi<AukletIoBrokers, Json> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AukletIoBrokers.class);

    private final String url;

    /**
     * <p>Constructor.</p>
     *
     * @param agent the Auklet agent object.
     * @throws AukletException if the underlying config file cannot be obtained from the filesystem/API,
     * or if it cannot be written to disk.
     */
    public AukletIoBrokers(Auklet agent) throws AukletException {
        super(agent);
        Json config = this.loadConfig();
        this.url = "ssl://" + config.at("brokers").asString() + ":" + config.at("port").asString();
    }

    @Override
    public String getName() {
        return "brokers";
    }

    /**
     * <p>Returns the MQTT connection URL</p>
     *
     * @return never {@code null}.
     */
    public String getUrl() { return this.url; }

    @Override
    protected Json readFromDisk() {
        try {
            return Json.make(this.getStringFromDisk());
        } catch (IOException | IllegalArgumentException e) {
            LOGGER.warn("Could not read broker config from disk, will re-download from API", e);
            return null;
        }
    }

    @Override
    protected Json fetchFromApi() throws AukletException {
        try {
            Request.Builder request = new Request.Builder()
                    .url(this.agent.getBaseUrl() + "/private/devices/config/").get()
                    .header("Content-Type", "application/json; charset=utf-8");
            try (Response response = this.agent.api(request)) {
                String responseJson = response.body().string();
                if (response.isSuccessful()) {
                    return Json.make(responseJson);
                } else {
                    throw new AukletException(String.format("Error while getting broker config: %s: %s", response.message(), responseJson));
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new AukletException("Could not get broker config", e);
        }
    }

    @Override
    protected void writeToDisk(Json contents) throws AukletException {
        this.saveStringToDisk(contents.toString());
    }

}
