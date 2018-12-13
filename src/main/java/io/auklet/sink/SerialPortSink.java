package io.auklet.sink;

import io.auklet.Auklet;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;

import java.io.IOException;
import java.io.OutputStream;

/** <p>An Auklet data sink backed by a named serial port.</p> */
public final class SerialPortSink extends AbstractSink {

    private final SerialPort port;
    private final OutputStream out;

    /**
     * <p>Constructs the serial data sink and opens the underlying serial port.</p>
     *
     * @param agent the Auklet agent object.
     * @throws SinkInitializationException if the serial port does not exist or is already in use, or if the
     * serial port's underlying output stream cannot be obtained.
     */
    public SerialPortSink(Auklet agent) throws SinkInitializationException {
        super(agent);
        String appName = "auklet:" + this.agent.getAppId();
        try {
            this.port = (SerialPort) CommPortIdentifier.getPortIdentifier(this.agent.getSerialPort()).open(appName, 1000);
        } catch (NoSuchPortException | PortInUseException e) {
            throw new SinkInitializationException("Could not initialize serial port sink", e);
        }
        try {
            this.out = this.port.getOutputStream();
        } catch (IOException e) {
            this.port.close();
            throw new SinkInitializationException("Could not initialize serial port sink", e);
        }
    }

    /**
     * <p>Sends the given throwable as an Auklet event object, wrapped further inside another MessagePack
     * map with 2 elements: the name of the target MQTT topic and the event payload.</p>
     */
    @Override
    public void send(Throwable throwable) throws SinkException {
        try {
            this.msgpack.packMapHeader(2)
                    .packString("topic").packString(this.agent.getDeviceAuth().getMqttEventsTopic())
                    .packString("payload"); // The value will be set by calling super.send().
        } catch (IOException e) {
            throw new SinkException("Could not assemble event message", e);
        }
        super.send(throwable);
    }

    @Override
    public void send(byte[] bytes) throws SinkException {
        try {
            this.out.write(bytes);
            this.out.flush();
        } catch (IOException e) {
            throw new SinkException("Could not write data to serial port", e);
        }
    }

    @Override
    public void shutdown() throws SinkException {
        super.shutdown();
        this.port.close(); // implicitly closes this.out
    }

}
