package io.auklet.config;

import io.auklet.Auklet;
import io.auklet.AukletException;
import mjson.Json;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * <p>Base class of all Auklet agent JSON config files that are sourced from the API.</p>
 */
public abstract class AbstractJsonConfigFileFromApi extends AbstractConfigFileFromApi<Json> {

    /**
     * <p>Constructor.</p>
     *
     * @param agent the Auklet agent object.
     */
    protected AbstractJsonConfigFileFromApi(Auklet agent) {
        super(agent);
    }

    protected final Json makeJsonRequest(Request.Builder request) throws AukletException {
        try (Response response = this.agent.getApi().doRequest(request)) {
            String responseString = response.body().string();
            if (response.isSuccessful()) {
                return Json.make(responseString);
            } else {
                throw new AukletException(String.format("Error while getting Auklet JSON config file '%s': %s: %s", this.getName(), response.message(), responseString));
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new AukletException(String.format("Error while getting Auklet JSON config file '%s'", this.getName()), e);
        }
    }

}
