package io.auklet;

import io.auklet.Auklet;

import org.junit.jupiter.api.Test;

import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import static org.junit.jupiter.api.Assertions.*;


class TestAuklet {
    private TestLogger logger = TestLoggerFactory.getTestLogger(Auklet.class);

    @Test void testSend() {
        Auklet.send(null);
        String loggingResult = logger.getLoggingEvents().asList().toString();
        assertEquals(true, loggingResult.contains("Ignoring send request for null throwable."));
        TestLoggerFactory.clear();
    }
}