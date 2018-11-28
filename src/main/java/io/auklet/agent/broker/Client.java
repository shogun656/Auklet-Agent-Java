package io.auklet.agent.broker;

public interface Client {
    void sendEvent(String topic, byte[] bytesToSend);
    void shutdown();
}
