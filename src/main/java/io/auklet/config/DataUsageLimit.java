package io.auklet.config;

import io.auklet.Auklet;
import io.auklet.AukletException;
import mjson.Json;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * <p>The <i>data usage limit file</i> contains the configuration values, for this agent's app ID,
 * that control how much data the agent emits to the sink.</p>
 */
public final class DataUsageLimit extends AbstractJsonConfigFileFromApi {

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
     * @throws AukletException if the underlying config file cannot be obtained from the filesystem/API,
     * or if it cannot be written to disk.
     */
    public DataUsageLimit() throws AukletException {
        Json config = this.loadConfig();
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
            Json config = this.fetchFromApi();
            this.writeToDisk(config);
            this.updateConfig(config);
        } catch (AukletException e) {
            LOGGER.warn("Could not refresh data usage limit config from API", e);
        }
    }

    @Override
    protected Json readFromDisk() {
        try {
            return Json.make(this.getStringFromDisk()).at("config");
        } catch (IOException | IllegalArgumentException e) {
            LOGGER.warn("Could not read data usage limits file from disk, will re-download from API", e);
            return null;
        }
    }

    @Override
    protected Json fetchFromApi() throws AukletException {
        String apiSuffix = String.format("/private/devices/%s/app_config/", this.getAgent().getAppId());
        Request.Builder request = new Request.Builder()
                .url(this.getAgent().getBaseUrl() + apiSuffix).get()
                .header("Content-Type", "application/json; charset=utf-8");
        return this.makeJsonRequest(request).at("config");
    }

    @Override
    protected void writeToDisk(Json contents) throws AukletException {
        this.saveStringToDisk(contents.toString());
    }

    /**
     * <p>Updates this object with the config values from the JSON.</p>
     *
     * @param config never {@code null}.
     */
    private void updateConfig(Json config) {
        this.emissionPeriod = config.at("emission_period").asLong() * SECONDS_TO_MILLISECONDS;
        this.storageLimit = config.at("storage").at("storage_limit", 0L).asLong() * MEGABYTES_TO_BYTES;
        this.cellularDataLimit = config.at("data").at("cellular_data_limit", 0L).asLong() * MEGABYTES_TO_BYTES;
        this.cellPlanDate = config.at("data").at("normalized_cell_plan_date").asInteger();
    }

}
