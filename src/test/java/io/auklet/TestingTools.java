package io.auklet;

import io.auklet.config.DeviceAuth;
import io.auklet.core.AukletDaemonExecutor;
import io.auklet.core.DataUsageMonitor;
import io.auklet.platform.JavaPlatform;
import io.auklet.util.ThreadUtil;
import mjson.Json;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class TestingTools {

    /*JSON Config testing declaration*/
    protected Json jsonConfig = Json.object()
            .set("organization", "organization_value")
            .set("client_id", "client_id_value")
            .set("id", "id_value")
            .set("client_password", "client_password_value");

    /*JSON Data Limits testing declaration*/
    private Json jsonFeatures = Json.object()
            .set("performance_metrics", false)
            .set("user_metrics", false);

    private Json jsonStorage = Json.object()
            .set("storage_limit", 100);

    private Json jsonData = Json.object()
            .set("cellular_data_limit", 100)
            .set("normalized_cell_plan_date", 100);

    private Json jsonDataLimitsConfig = Json.object()
            .set("features", jsonFeatures)
            .set("emission_period", 100)
            .set("storage", jsonStorage)
            .set("data", jsonData);

    protected Json jsonDataLimits = Json.object()
            .set("config", jsonDataLimitsConfig);

    protected Json newJsonDataLimits = Json.object()
            .set("config", jsonDataLimitsConfig);

    /*JSON Brokers testing declaration*/
    protected Json jsonBrokers = Json.object()
            .set("brokers", "0.0.0.0")
            .set("port", "0000");

    protected Auklet aukletConstructor() throws AukletException {
        Auklet mocked = mock(Auklet.class);
        given(mocked.getAppId()).willReturn("0123456789101112");
        given(mocked.getDeviceAuth()).willReturn(new DeviceAuth());
        given(mocked.getUsageMonitor()).willReturn(new DataUsageMonitor());
        given(mocked.getIpAddress()).willReturn("");
        given(mocked.getPlatform()).willReturn(new JavaPlatform());
        given(mocked.getConfigDir()).willReturn(new File(".auklet").getAbsoluteFile());
        given(mocked.getMqttThreads()).willReturn(2);
        given(mocked.getMacHash()).willReturn("");
        return mocked;
    }
}