package io.auklet.sink;

import io.auklet.Auklet;
import io.auklet.AukletException;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;

import java.io.IOException;
import java.io.OutputStream;

/** <p>An Auklet data sink backed by a named serial port.</p> */
public final class SerialPortSink extends AbstractSink {

    private SerialPort port;
    private OutputStream out;

    /**
     * <p>Constructs the serial data sink and opens the underlying serial port.</p>
     *
     * @throws AukletException if the serial port does not exist or is already in use, or if the
     * serial port's underlying output stream cannot be obtained.
     */
    @Override
    public void setAgent(Auklet agent) throws AukletException {
        try {
            String appName = "auklet:" + this.getAgent().getAppId();
            this.port = (SerialPort) CommPortIdentifier.getPortIdentifier(this.getAgent().getSerialPort()).open(appName, 1000);
        } catch (NoSuchPortException | PortInUseException e) {
            throw new AukletException("Could not initialize serial port sink", e);
        }
        try {
            this.out = this.port.getOutputStream();
        } catch (IOException e) {
            this.port.close();
            throw new AukletException("Could not initialize serial port sink", e);
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
                    .packString("topic").packString(this.getAgent().getDeviceAuth().getMqttEventsTopic())
                    .packString("payload"); // The value will be set by calling super.write().
        } catch (AukletException | IOException e) {
            throw new SinkException("Could not assemble event message", e);
        }
        super.send(throwable);
    }

    @Override
    public void write(byte[] bytes) throws SinkException {
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
