//package io.auklet.config;
//
//import io.auklet.TestingTools;
//import io.auklet.Auklet;
//import io.auklet.AukletException;
//import io.auklet.Config;
//import mjson.Json;
//import okhttp3.Request;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.TestInstance;
//import org.mockito.Mockito;
//import org.mockito.internal.util.reflection.FieldSetter;
//
//import java.io.BufferedInputStream;
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.io.UnsupportedEncodingException;
//import java.lang.reflect.Constructor;
//import java.lang.reflect.InvocationTargetException;
//import java.security.*;
//import java.security.cert.*;
//import java.security.cert.Certificate;
//
//import static org.mockito.ArgumentMatchers.any;
//
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//class TestAukletIoCert extends TestingTools {
//    private AukletIoCert aukletIoCert;
//    private X509Certificate mockedCert;
//
//    @BeforeAll void setup() throws AukletException, NoSuchFieldException, ClassCastException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, RuntimeException {
//        aukletIoCert = new AukletIoCert();
////        aukletIoCert.start(aukletConstructor());
//        mockedCert = getCertificate();
//        FieldSetter.setField(aukletIoCert.getClass(), aukletIoCert.getClass().getDeclaredField("cert"), mockedCert);
//    }
//
//    @Test void testGetName() {
//
//    }
//}
