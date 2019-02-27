//TODO: Finish test module
package io.auklet.config;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataUsageTrackerTest {
    DataUsageTracker dataUsageTracker;
    @BeforeAll void setup() {
        dataUsageTracker = new DataUsageTracker();
    }

    @Test void testGetName() {

    }
}
