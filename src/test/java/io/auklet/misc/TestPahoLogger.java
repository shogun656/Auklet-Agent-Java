package io.auklet.misc;

import io.auklet.Auklet;
import io.auklet.misc.PahoLogger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

import static org.eclipse.paho.client.mqttv3.logging.Logger.*;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS) class TestPahoLogger {
    private PahoLogger pahoLogger = new PahoLogger();
    private TestLogger logger = TestLoggerFactory.getTestLogger("TestPahoLogger");

    @BeforeAll void setup() {
        this.pahoLogger.initialise(null, "TestPahoLogger", null);
    }

    @AfterEach void clearTestLoggerFactory() {
        TestLoggerFactory.clear();
    }

    @Test void testInitialise() {
        PahoLogger pahoLogger = new PahoLogger();
        ResourceBundle resourceBundle = new ResourceBundle() {
            @Override
            protected Object handleGetObject(String key) {
                return null;
            }

            @Override
            public Enumeration<String> getKeys() {
                return null;
            }
        };
        try {
            pahoLogger.initialise(resourceBundle, null, "1");
        } catch (IllegalArgumentException e) {
            assertEquals("java.lang.IllegalArgumentException: loggerID is null.", e.toString());
        }
    }

    @Test void testIsLoggable() {
        assertEquals(true, pahoLogger.isLoggable(SEVERE));
        assertEquals(true, pahoLogger.isLoggable(WARNING));
        assertEquals(true, pahoLogger.isLoggable(INFO));
        assertEquals(true, pahoLogger.isLoggable(CONFIG));
        assertEquals(true, pahoLogger.isLoggable(FINEST));
        assertEquals(false, pahoLogger.isLoggable(0));
    }

    @Test void testSevere() {
        String message = "TestSevere";
        pahoLogger.severe("SourceClass", "SourceMethod", message);
        assertEquals(true, logger.getLoggingEvents().toString().contains(message));

        pahoLogger.severe("SourceClass", "SourceMethod", message, null);
        assertEquals(true, logger.getLoggingEvents().toString().contains(message));

        pahoLogger.severe("SourceClass", "SourceMethod", message, null, null);
        assertEquals(true, logger.getLoggingEvents().toString().contains(message));
    }

    @Test void testWarning() {
        String message = "TestWarning";
        pahoLogger.warning("SourceClass", "SourceMethod", message);
        assertEquals(true, logger.getLoggingEvents().toString().contains(message));

        pahoLogger.warning("SourceClass", "SourceMethod", message, null);
        assertEquals(true, logger.getLoggingEvents().toString().contains(message));

        pahoLogger.warning("SourceClass", "SourceMethod", message, null, null);
        assertEquals(true, logger.getLoggingEvents().toString().contains(message));
    }

    @Test void testInfo() {
        String message = "TestInfo";
        pahoLogger.info("SourceClass", "SourceMethod", message);
        assertEquals(true, logger.getLoggingEvents().toString().contains(message));

        pahoLogger.info("SourceClass", "SourceMethod", message, null);
        assertEquals(true, logger.getLoggingEvents().toString().contains(message));

        pahoLogger.info("SourceClass", "SourceMethod", message, null, null);
        assertEquals(true, logger.getLoggingEvents().toString().contains(message));
    }

    @Test void testConfig() {
        String message = "TestConfig";
        pahoLogger.config("SourceClass", "SourceMethod", message);
        assertEquals(true, logger.getLoggingEvents().toString().contains(message));

        pahoLogger.config("SourceClass", "SourceMethod", message, null);
        assertEquals(true, logger.getLoggingEvents().toString().contains(message));

        pahoLogger.config("SourceClass", "SourceMethod", message, null, null);
        assertEquals(true, logger.getLoggingEvents().toString().contains(message));
    }

    @Test void testFine() {
        String message = "TestFine";
        pahoLogger.fine("SourceClass", "SourceMethod", message);
        assertEquals(true, logger.getLoggingEvents().toString().contains(message));

        pahoLogger.fine("SourceClass", "SourceMethod", message, null);
        assertEquals(true, logger.getLoggingEvents().toString().contains(message));

        pahoLogger.fine("SourceClass", "SourceMethod", message, null, null);
        assertEquals(true, logger.getLoggingEvents().toString().contains(message));
    }

    @Test void testFiner() {
        String message = "TestFiner";
        pahoLogger.finer("SourceClass", "SourceMethod", message);
        assertEquals(true, logger.getLoggingEvents().toString().contains(message));

        pahoLogger.finer("SourceClass", "SourceMethod", message, null);
        assertEquals(true, logger.getLoggingEvents().toString().contains(message));

        pahoLogger.finer("SourceClass", "SourceMethod", message, null, null);
        assertEquals(true, logger.getLoggingEvents().toString().contains(message));
    }

    @Test void testFinest() {
        String message = "TestFinest";
        pahoLogger.finest("SourceClass", "SourceMethod", message);
        assertEquals(true, logger.getLoggingEvents().toString().contains(message));

        pahoLogger.finest("SourceClass", "SourceMethod", message, null);
        assertEquals(true, logger.getLoggingEvents().toString().contains(message));

        pahoLogger.finest("SourceClass", "SourceMethod", message, null, null);
        assertEquals(true, logger.getLoggingEvents().toString().contains(message));
    }

    @Test void testLog() {
        String message = "TestLog";
        pahoLogger.log(0, "SourceClass", "SourceMethod", message, null, null);
        assertEquals(false, logger.getLoggingEvents().asList().toString().contains(message));
    }

    @Test void testTrace() {
        String message = "TestTrace";
        pahoLogger.trace(0, "SourceClass", "SourceMethod", message, null, null);
        assertEquals(false, logger.getLoggingEvents().asList().toString().contains(message));
    }
}
