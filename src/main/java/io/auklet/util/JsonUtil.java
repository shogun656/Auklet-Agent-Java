package io.auklet.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.auklet.AukletException;
import mjson.Json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/** <p>Utility methods related to JSON manipulation.</p> */
public final class JsonUtil {

    private JsonUtil() {}

    /**
     * <p>Parses the given JSON string into a JSON object.</p>
     *
     * @param json the string to parse. If {@code null} or empty, the returned JSON object
     * will be empty.
     * @return never {@code null}, but may be a representation of the JSON "null" element.
     */
    @NonNull
    public static Json readJson(@Nullable String json) {
        return Json.read(Util.orElseNullEmpty(json, "{}"));
    }

    /**
     * <p>Validates the given JSON config object against the schema defined for the given Java class.</p>
     *
     * @param json never {@code null}.
     * @param clazz the classname of the JSON schema. Never {@code null}.
     * @return the input JSON object.
     * @throws AukletException if schema validation fails.
     */
    @NonNull public static Json validateJson(@NonNull Json json, @NonNull String clazz) throws AukletException {
        if (json == null) throw new AukletException("Input is null.");
        Json schemaValidation = getJsonSchema(clazz).validate(json);
        if (schemaValidation.is("ok", true)) return json;
        List<Json> errors = schemaValidation.at("errors").asJsonList();
        StringBuilder errorString = new StringBuilder();
        for (Json error : errors) {
            errorString.append('\n').append(error.asString());
        }
        throw new AukletException(String.format("Errors while parsing Auklet JSON config file '%s': %s", clazz, errorString.toString()));
    }

    /**
     * <p>Returns the JSON schema for the given Java class.</p>
     *
     * @param clazz the classname of the JSON schema. Never {@code null}.
     * @return never {@code null}.
     * @throws AukletException if the schema could not be read.
     */
    @NonNull public static Json.Schema getJsonSchema(@NonNull String clazz) throws AukletException {
        if (clazz == null) throw new AukletException("Schema class is null.");
        String schemaPath = clazz.replace('.', '/') + ".schema.json";
        try (InputStream schemaStream = Util.class.getClassLoader().getResourceAsStream(schemaPath)) {
            if (schemaStream == null) throw new AukletException("JSON schema stream is null.");
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = schemaStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            return Json.schema(Json.read(new String(buffer.toByteArray(), Util.UTF_8)));
        } catch (SecurityException | IOException e) {
            throw new AukletException("Could not read JSON schema.", e);
        }
    }

}
