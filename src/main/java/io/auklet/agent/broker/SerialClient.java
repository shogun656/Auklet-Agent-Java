package io.auklet.agent.broker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class SerialClient implements Client {
    private static Logger logger = LoggerFactory.getLogger(SerialClient.class);
    private SerialPort comm;
    private OutputStream stream;

    public SerialClient(String portName) throws  NoSuchPortException, PortInUseException, IOException {
        comm = (SerialPort) CommPortIdentifier.getPortIdentifier(portName).open("AukletPort", 1000);
        stream = comm.getOutputStream();
    }

    @Override
    public void sendEvent(String topic, byte[] bytesToSend) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("topic", topic);
            map.put("payload", bytesToSend);

            ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory());
            byte[] bytes = objectMapper.writeValueAsBytes(map);
            stream.write(bytes);
            logger.info("Event was sent");
        } catch (IOException e) {
            logger.error("Event was unable to be sent", e);
        }
    }

    @Override
    public void shutdown() {
        try {
            stream.close();
            comm.close();
        } catch (IOException e) {
            logger.error("Error while shutting down Serial Client", e);
        }
    }
}
