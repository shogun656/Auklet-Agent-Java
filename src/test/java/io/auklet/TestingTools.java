package io.auklet;

import mjson.Json;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class TestingTools {

    protected Json jsonConfig = Json.object().set("organization", "organization_value")
                                             .set("client_id", "client_id_value")
                                             .set("id", "id_value")
                                             .set("client_password", "client_password_value");



    private Json jsonStorage = Json.object().set("storage_limit", 100);
    private Json jsonData = Json.object().set("cellular_data_limit", 100)
                                         .set("normalized_cell_plan_date", 100);
    private Json jsonFeatures = Json.object().set("performance_metrics", false)
                                             .set("user_metrics", false);
    private Json jsonDataLimitsConfig = Json.object().set("features", jsonFeatures)
                                                     .set("emission_period", 100)
                                                     .set("storage", jsonStorage)
                                                     .set("data", jsonData);
    protected Json jsonDataLimits = Json.object().set("config", jsonDataLimitsConfig);

    protected Json newJsonDataLimits = Json.object().set("config", jsonDataLimitsConfig);

    protected Auklet aukletConstructor() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, RuntimeException {
        Config config = new Config().setAppId("0123456789101112")
                .setApiKey("123");

        Constructor<Auklet> aukletConstructor = Auklet.class.getDeclaredConstructor(config.getClass());
        aukletConstructor.setAccessible(true);
        return aukletConstructor.newInstance(config);
    }
}