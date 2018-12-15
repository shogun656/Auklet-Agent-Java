package io.auklet.sink;

import io.auklet.Auklet;
import io.auklet.AukletException;
import io.auklet.misc.HasAgent;
import io.auklet.misc.Util;
import io.auklet.jvm.OSMX;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Optional;
import java.util.UUID;

/**
 * <p>Base class of all Auklet agent data sinks. Each implementation provides a {@link MessagePacker} that
 * is used to construct MessagePack payloads that are then sent to the underlying output in the
 * {@link MessagePacker} object (e.g. an {@code OutputStream}).</p>
 */
public abstract class AbstractSink extends HasAgent implements Sink {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSink.class);
    private final Object lock = new Object();
    protected final MessageBufferPacker msgpack = MessagePack.newDefaultBufferPacker();

    @Override
    public void send(Throwable throwable) throws SinkException {
        if (throwable == null) return;
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        synchronized (this.lock) {
            // Assemble the complete message.
            this.msgpack.clear();
            try {
                this.initMessage(10);
                this.msgpack
                        .packString("timestamp").packLong(System.currentTimeMillis())
                        .packString("excType").packString(throwable.toString())
                        .packString("stackTrace").packMapHeader(stackTrace.length);
                for (StackTraceElement ste : stackTrace) {
                    int lineNumber = ste.getLineNumber();
                    this.msgpack
                            .packString("functionName").packString(ste.getMethodName())
                            .packString("className").packString(ste.getClassName())
                            .packString("filePath").packString(Util.defaultValue(ste.getFileName(), "unknown"))
                            // Normalize all negative return values.
                            .packString("lineNumber").packInt(lineNumber < 0 ? -1 : lineNumber);
                }
            } catch (IOException e) {
                throw new SinkException("Could not assemble event message", e);
            }
            this.writeToSink();
        }
    }

    /**
     * <p>Writes the contents of the MessagePacker to the underlying sink, unless it is empty and unless
     * it is so large that it would exceed the data usage limit.</p>
     *
     * @throws SinkException if an error occurs while writing the data. This exception is <i>not</i> thrown
     * if the data usage limit would be exceeded; in that case, this method no-ops.
     */
    private final void writeToSink() throws SinkException {
        // If the message is not empty, and if it will not push the device over the limit,
        // write the message and update the usage monitor.
        try {
            this.msgpack.flush();
            byte[] payload = this.msgpack.toByteArray();
            if (payload == null) payload = new byte[0];
            int payloadSize = payload.length;
            boolean payloadWillExceedLimit = this.getAgent().getUsageMonitor().willExceedLimit(payloadSize);
            if (!payloadWillExceedLimit) {
                this.write(payload);
                this.getAgent().getUsageMonitor().addMoreData(payloadSize);
            }
        } catch (AukletException | IOException e) {
            throw new SinkException("Could not write event message", e);
        }
    }

    /**
     * <p>Writes the given byte array to the underlying data sink.</p>
     *
     * @param bytes no-op if null or empty.
     * @throws SinkException if the data cannot be written.
     */
    protected abstract void write(byte[] bytes) throws SinkException;

    @Override
    public void shutdown() throws SinkException {
        synchronized (this.lock) {
            try {
                this.msgpack.close();
            } catch (IOException e) {
                throw new SinkException("Error while shutting down MessagePacker", e);
            }
        }
    }

    /**
     * <p>Starts assembling an Auklet-compatible MessagePack message, which is defined as a MessagePack
     * map with at least 7 elements in it.</p>
     *
     * @param mapSize the size of the map message.
     * @throws SinkException if the map size is less than 7, or if an error occurs while assembling the
     * message payload.
     */
    private void initMessage(int mapSize) throws SinkException {
        if (mapSize < 7) throw new SinkException("Message size is too small.");
        try {
            this.msgpack.packMapHeader(mapSize)
                    .packString("id").packString(UUID.randomUUID().toString())
                    .packString("application").packString(this.getAgent().getAppId())
                    .packString("macAddressHash").packString(this.getAgent().getMacHash())
                    .packString("publicIP").packString(this.getAgent().getIpAddress())
                    .packString("systemMetrics");
            this.addSystemMetrics();
            this.msgpack.packString("agentVersion").packString(Auklet.VERSION)
                    .packString("device").packString(this.getAgent().getDeviceAuth().getClientUsername());
        } catch (AukletException | IOException | IllegalArgumentException e) {
            throw new SinkException("Error while assembling msgpack payload", e);
        }
    }

    /**
     * <p>Adds JVM system metrics to the current position in the given MessagePacker as a map object.</p>
     *
     * @throws IllegalArgumentException if the MessagePacker is {@code null}.
     * @throws SinkException if an error occurs while assembling the message.
     */
    private void addSystemMetrics() throws SinkException {
        try {
            this.msgpack.packMapHeader(4);
            // Calculate memory usage.
            Optional<Long> freeMem = OSMX.BEAN.getFreePhysicalMemorySize();
            Optional<Long> totalMem = OSMX.BEAN.getTotalPhysicalMemorySize();
            double memUsage;
            if (freeMem.isPresent() && totalMem.isPresent()) {
                memUsage = 100 * (1 - ((double) freeMem.get() / (double) totalMem.get()));
            } else {
                memUsage = 0;
            }
            this.msgpack.packString("memoryUsage").packDouble(memUsage);
            // Calculate CPU usage.
            double cpuUsage;
            Optional<Double> sysLoadAvg = OSMX.BEAN.getSystemLoadAverage();
            if (sysLoadAvg.isPresent()) {
                cpuUsage = 100 * (sysLoadAvg.get() / OSMX.BEAN.getAvailableProcessors());
            } else {
                cpuUsage = 0;
            }
            this.msgpack.packString("cpuUsage").packDouble(cpuUsage);
            // Add other system metrics.
            this.msgpack.packString("outboundNetwork").packDouble(0);
            this.msgpack.packString("inboundNetwork").packDouble(0);
        } catch (IOException e) {
            throw new SinkException("Error while assembling msgpack payload", e);
        }
    }

}
