package io.auklet.agent.broker;

import java.util.concurrent.ScheduledExecutorService;

public interface Client {
    void sendEvent(byte[] bytesToSend);
    void shutdown(ScheduledExecutorService threadPool);
}
