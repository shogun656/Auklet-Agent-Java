package io.auklet.config;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.auklet.AukletException;
import mjson.Json;
import net.jcip.annotations.NotThreadSafe;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * <p>Base class of all Auklet agent JSON config files that are sourced from the API.</p>
 */
@NotThreadSafe
public abstract class AbstractJsonConfigFileFromApi extends AbstractConfigFileFromApi<Json> {

    /**
     * <p>Submits a request to the Auklet API and returns the response as a JSON object.</p>
     *
     * @param request the API request. Never {@code null}.
     * @return never {@code null}.
     * @throws AukletException if the request is {@code null}, or if the request fails or has an error.
     */
    @NonNull protected final Json makeJsonRequest(@NonNull Request.Builder request) throws AukletException {
        if (request == null) throw new AukletException("JSON HTTP request is null");
        try (Response response = this.getAgent().getApi().doRequest(request)) {
            String responseString = response.body().string();
            if (response.isSuccessful()) {
                return this.validate(Json.read(responseString));
            } else {
                throw new AukletException(String.format("Error while getting Auklet JSON config file '%s': %s: %s", this.getName(), response.message(), responseString));
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new AukletException(String.format("Error while getting Auklet JSON config file '%s'", this.getName()), e);
        }
    }

    /**
     * <p>Returns the JSON schema for this config file.</p>
     *
     * @return never {@code null}.
     */
    @NonNull protected abstract Json.Schema getSchema();

    /**
     * <p>Validates the given JSON config object against the schema defined for this Java class.</p>
     *
     * @param json never {@code null}.
     * @return the input object.
     * @throws AukletException if schema validation fails.
     */
    @NonNull protected final Json validate(@NonNull Json json) throws AukletException {
        if (json == null) throw new AukletException("Input is null");
        Json schemaValidation = this.getSchema().validate(json);
        if (schemaValidation.is("ok", true)) return json;
        else throw new AukletException(String.format("Errors while parsing Auklet JSON config file '%s': %s", this.getName(), schemaValidation.at("errors").toString()));
    }

}
