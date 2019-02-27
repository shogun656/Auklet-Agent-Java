// TODO: Fix test module
//package io.auklet.platform;
//
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.TestInstance;
//import org.msgpack.core.MessagePacker;
//
//import java.io.IOException;
//import java.nio.file.Paths;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//class AbstractPlatformTest {
//    private AbstractPlatform abstractPlatform;
//
//    @BeforeAll void setup() {
//        abstractPlatform = new AbstractPlatform() {
//            @Override protected List<String> getPossibleConfigDirs(String fromConfig) {
//                List<String> list = new ArrayList<>();
//                if (fromConfig == null) {
//                    return list;
//                }
//                list.add(fromConfig);
//                return list;
//            }
//
//            @Override public void addSystemMetrics(MessagePacker msgpack) throws IOException {}
//        };
//    }
//
//    @Test void testObtainConfigDir() {
//        System.out.println(Paths.get("").toAbsolutePath().toString());
//        System.out.print(abstractPlatform.obtainConfigDir(null));
//        System.out.print(abstractPlatform.obtainConfigDir("."));
//        System.out.print(abstractPlatform.obtainConfigDir("tmp"));
//    }
//}
