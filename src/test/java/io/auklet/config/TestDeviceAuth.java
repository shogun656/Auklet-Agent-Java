package io.auklet.config;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.auklet.Auklet;
import io.auklet.AukletException;
import io.auklet.Config;
import io.auklet.config.DeviceAuth;
import io.auklet.config.AbstractConfigFileFromApi;

import mjson.Json;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestDeviceAuth {
    private DeviceAuth deviceAuth;
    private Config config;
    private Json jsonConfig;

    @BeforeAll void setup() throws AukletException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, RuntimeException {
        deviceAuth = new DeviceAuth();

        jsonConfig = Json.object().set("organization", 0)
                                  .set("client_id", 0)
                                  .set("id", 0)
                                  .set("client_password", 0);

        config = new Config();
        config.setAppId("0123456789101112");
        config.setApiKey("123");

        Constructor<Auklet> aukletConstructor = Auklet.class.getDeclaredConstructor(config.getClass());
        aukletConstructor.setAccessible(true);
        Auklet auklet = aukletConstructor.newInstance(config);

//        final AbstractConfigFileFromApi abstractConfigFileFromApi = Mockito.mock(AbstractConfigFileFromApi.class);
//        Mockito.when(abstractConfigFileFromApi.loadConfig()).thenReturn(mockedLoadConfig());

        final DeviceAuth deviceAuth = Mockito.mock(DeviceAuth.class);
        Mockito.when(deviceAuth.fetchFromApi()).thenReturn(Json.object());
        Mockito.when(deviceAuth.loadConfig()).thenReturn(jsonConfig);

        System.out.print("Before start");
        deviceAuth.start(auklet);
    }

    @Test void testGetName() {
            assertEquals("AukletAuth", deviceAuth.getName());
    }

    @Test void testGetOrganizationId() {
        System.out.print(deviceAuth.getOrganizationId());
    }

    @Test void testGetClientId() {

    }

    @Test void testGetClientUsername() {

    }

    @Test void testGetClientPassword() {

    }

    @Test void testGetMqttEventsTopic() {

    }
}
