//package io.auklet.sink;
//
//import io.auklet.Auklet;
//import io.auklet.AukletException;
//import io.auklet.Config;
//import io.auklet.config.AukletIoCert;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.TestInstance;
//import org.mockito.Mockito;
//import org.mockito.MockitoAnnotations;
//import org.powermock.api.mockito.PowerMockito;
//
//import java.lang.reflect.Constructor;
//import java.lang.reflect.InvocationTargetException;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//class TestAukletIoSink {
//    private AukletIoSink aukletIoSink;
//
//    @BeforeAll void setup() throws AukletException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, RuntimeException, Exception {
//
//        Config config = new Config().setAppId("0123456789101112")
//                .setApiKey("123");
//
//        Constructor<Auklet> aukletConstructor = Auklet.class.getDeclaredConstructor(config.getClass());
//        aukletConstructor.setAccessible(true);
//        Auklet auklet = aukletConstructor.newInstance(config);
//
//        aukletIoSink = new AukletIoSink();
//
//        aukletIoSink.start(auklet);
//    }
//
//    @Test void testWrite() throws AukletException {
//
//    }
//}
