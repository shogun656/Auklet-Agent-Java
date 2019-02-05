package io.auklet;

import io.auklet.AukletException;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TestAukletException {
    @Test void TestEmptyAukletException() {
        AukletException aukletException = new AukletException();
        try {
            throw aukletException;
        } catch (AukletException e) {
            assertEquals("io.auklet.AukletException", e.toString());
        }
    }

    @Test void TestStringAukletException() {
        AukletException aukletException = new AukletException("message");
        try {
            throw aukletException;
        } catch (AukletException e) {
            assertEquals("io.auklet.AukletException: message", e.toString());
        }
    }

    @Test void TestStringThrowableAukletException() {
        Throwable cause = new Throwable();
        AukletException aukletException = new AukletException("message", cause);
        try {
            throw aukletException;
        } catch (AukletException e) {
            assertEquals("io.auklet.AukletException: message", e.toString());
        }
    }

    @Test void TestThrowableAukletException() {
        Throwable cause = new Throwable();
        AukletException aukletException = new AukletException(cause);
        try {
            throw aukletException;
        } catch (AukletException e) {
            assertEquals("io.auklet.AukletException: java.lang.Throwable", e.toString());
        }
    }
}
