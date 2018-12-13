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
 * <p>The <i>data usage limit file</i> contains the configuration values, for this agent's app ID,
 * that control how much data the agent emits to the sink.</p>
 */
public final class DataUsageLimit extends AbstractConfigFileFromApi<DataUsageLimit, JsonObject> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataUsageLimit.class);
    private static final Long MEGABYTES_TO_BYTES = 1000000L;
    private static final Long SECONDS_TO_MILLISECONDS = 1000L;

    private long emissionPeriod;
    private long storageLimit;
    private long cellularDataLimit;
    private int cellPlanDate;

    /**
     * <p>Constructor.</p>
     *
     * @param agent the Auklet agent object.
     * @throws AukletException if the underlying config file cannot be obtained from the filesystem/API,
     * or if it cannot be written to disk.
     */
    public DataUsageLimit(Auklet agent) throws AukletException {
        super(agent);
        JsonObject config = this.loadConfig();
        this.updateConfig(config);
    }

    @Override
    public String getName() {
        return "limits";
    }

    /**
     * <p>Returns the emission period.</p>
     *
     * @return the emission period.
     */
    public long getEmissionPeriod() {
        return this.emissionPeriod;
    }

    /**
     * <p>Returns the storage limit.</p>
     *
     * @return the storage limit.
     */
    public long getStorageLimit() {
        return this.storageLimit;
    }

    /**
     * <p>Returns the cellular data limit.</p>
     *
     * @return the cellular data limit.
     */
    public long getCellularDataLimit() {
        return this.cellularDataLimit;
    }

    /**
     * <p>Returns the cell plan date.</p>
     *
     * @return the cell plan date.
     */
    public int getCellPlanDate() {
        return this.cellPlanDate;
    }

    /** <p>Refreshes the data usage limit config from the API.</p> */
    public void refresh() {
        try {
            JsonObject config = this.fetchFromApi();
            this.writeToDisk(config);
            this.updateConfig(config);
        } catch (AukletException e) {
            LOGGER.warn("Could not refresh data usage limit config from API", e);
        }
    }

    @Override
    protected JsonObject readFromDisk() {
        try {
            JsonObject raw = (JsonObject) Jsoner.deserialize(this.getStringFromDisk());
            return (JsonObject) raw.get("config");
        } catch (IOException | JsonException e) {
            LOGGER.warn("Could not read data usage limits file from disk, will re-download from API", e);
            return null;
        }
    }

    @Override
    protected JsonObject fetchFromApi() throws AukletException {
        try {
            String apiSuffix = String.format("/private/devices/%s/app_config/", this.agent.getAppId());
            Request.Builder request = new Request.Builder()
                    .url(this.agent.getBaseUrl() + apiSuffix).get()
                    .header("Content-Type", "application/json; charset=utf-8");
            try (Response response = this.agent.api(request)) {
                String responseJson = response.body().string();
                if (response.isSuccessful()) {
                    return (JsonObject) Jsoner.deserialize(responseJson);
                } else {
                    throw new AukletException(String.format("Error while getting data usage limits: %s: %s", response.message(), responseJson));
                }
            }
        } catch (IOException | JsonException e) {
            throw new AukletException("Could not get data usage limits", e);
        }
    }

    @Override
    protected void writeToDisk(JsonObject contents) throws AukletException {
        this.saveStringToDisk(contents.toJson());
    }

    /**
     * <p>Updates this object with the config values from the JSON.</p>
     *
     * @param config never {@code null}.
     */
    private void updateConfig(JsonObject config) {
        this.emissionPeriod = (long) config.get("emission_period") * SECONDS_TO_MILLISECONDS;
        Long storageLimit = (Long) ((JsonObject) config.get("storage")).get("storage_limit");
        this.storageLimit = (storageLimit == null ? 0 : storageLimit) * MEGABYTES_TO_BYTES;
        Long cellularDataLimit = (Long) ((JsonObject) config.get("data")).get("cellular_data_limit");
        this.cellularDataLimit  = (cellularDataLimit == null ? 0 : cellularDataLimit) * MEGABYTES_TO_BYTES;
        this.cellPlanDate = (int) ((JsonObject) config.get("data")).get("normalized_cell_plan_date");
    }

}
