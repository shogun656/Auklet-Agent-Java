package io.auklet.config;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.Jsoner;
import io.auklet.Auklet;
import io.auklet.AukletException;
import okhttp3.Request;
import okhttp3.Response;
import com.github.cliftonlabs.json_simple.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * <p>This file contains MQTT connection information for sending data to {@code auklet.io}.</p>
 */
public final class AukletIoBrokers extends AbstractConfigFileFromApi<AukletIoBrokers, JsonObject> {

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
        JsonObject config = this.loadConfig();
        this.url = "ssl://" + (String) config.get("brokers") + ":" + (String) config.get("port");
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
    protected JsonObject readFromDisk() {
        try {
            return (JsonObject) Jsoner.deserialize(this.getStringFromDisk());
        } catch (IOException | JsonException e) {
            LOGGER.warn("Could not read broker config from disk, will re-download from API", e);
            return null;
        }
    }

    @Override
    protected JsonObject fetchFromApi() throws AukletException {
        try {
            Request.Builder request = new Request.Builder()
                    .url(this.agent.getBaseUrl() + "/private/devices/config/").get()
                    .header("Content-Type", "application/json; charset=utf-8");
            try (Response response = this.agent.api(request)) {
                String responseJson = response.body().string();
                if (response.isSuccessful()) {
                    return (JsonObject) Jsoner.deserialize(responseJson);
                } else {
                    throw new AukletException(String.format("Error while getting broker config: %s: %s", response.message(), responseJson));
                }
            }
        } catch (IOException | JsonException e) {
            throw new AukletException("Could not get broker config", e);
        }
    }

    @Override
    protected void writeToDisk(JsonObject contents) throws AukletException {
        this.saveStringToDisk(contents.toJson());
    }

}
