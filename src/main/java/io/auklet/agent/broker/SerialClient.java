package io.auklet.agent.broker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import purejavacomm.CommPort;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

public class SerialClient implements Client {

    private Boolean setUp;
    private CommPort comm;

    public SerialClient(String portName) {
        try {
            comm = CommPortIdentifier.getPortIdentifier(portName).open("AukletPort", 1000);
            setUp = true;
        } catch (NoSuchPortException | PortInUseException e) {
            setUp = false;
            // log error
        }
    }

    @Override
    public boolean isSetUp() {
        return setUp;
    }

    @Override
    public void sendEvent(String topic, byte[] bytesToSend) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("topic", topic);
            map.put("payload", bytesToSend);

            ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory());
            byte[] bytes = objectMapper.writeValueAsBytes(map);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } finally {

        }
    }

    @Override
    public void shutdown(ScheduledExecutorService threadPool) {

    }
}
