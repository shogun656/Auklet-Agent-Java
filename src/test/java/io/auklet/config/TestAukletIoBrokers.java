//package io.auklet.config;
//
//import io.auklet.Auklet;
//import io.auklet.Config;
//import io.auklet.config.AukletIoBrokers;
//
//import io.auklet.AukletException;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.TestInstance;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//
//import java.lang.reflect.Constructor;
//import java.lang.reflect.InvocationTargetException;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//final class TestAukletIoBrokers {
//    private AukletIoBrokers aukletIoBrokers;
//    private Auklet auklet;
//
//    @BeforeAll void setup() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
//        Constructor<Auklet> auklet = Auklet.class.getDeclaredConstructor();
//        auklet.setAccessible(true);
//        auklet.newInstance((Object[]) null);
//
//        aukletIoBrokers = new AukletIoBrokers();
//    }
//
//    @Test void testStart() throws AukletException {
//
//    }
//}
