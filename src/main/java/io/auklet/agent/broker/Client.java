package io.auklet.agent.broker;

import java.util.concurrent.ScheduledExecutorService;

public interface Client {
    boolean isSetUp();
    void sendEvent(String topic, byte[] bytesToSend);
    void shutdown(ScheduledExecutorService threadPool);
}
