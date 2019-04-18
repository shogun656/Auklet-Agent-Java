package io.auklet;

import java.math.BigInteger;
import io.auklet.AukletException;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import net.jcip.annotations.GuardedBy;

import java.io.*;
import java.util.*;


public class Datapoint {

    @GuardedBy("itself") protected final MessageBufferPacker msgpack = MessagePack.newDefaultBufferPacker();
    private final String dataType = "value";
    public byte[] dataValue;

    /**
     * <p>Initializes a Datapoint class which packs up the passed data into messagepack byte arrays</p>
     *
     * @param data data to be packed
     * @throws AukletException to wrap any underlying exceptions.
     */
    public Datapoint(boolean data) throws AukletException {
        synchronized (this.msgpack) {
            this.msgpack.clear();
            try {
                this.msgpack.packMapHeader(1)
                        .packString(this.dataType).packBoolean(data);
            } catch (IOException e) {
                throw new AukletException("Could not assemble datapoint message", e);
            }
            this.dataValue = this.msgpack.toByteArray();
        }
    }

    /**
     * <p>Initializes a Datapoint class which packs up the passed data into messagepack byte arrays</p>
     *
     * @param data data to be packed
     * @throws AukletException to wrap any underlying exceptions.
     */
    public Datapoint(byte data) throws AukletException {
        synchronized (this.msgpack) {
            this.msgpack.clear();
            try {
                this.msgpack.packMapHeader(1)
                        .packString(this.dataType).packByte(data);
            } catch (IOException e) {
                throw new AukletException("Could not assemble datapoint message", e);
            }
            this.dataValue = this.msgpack.toByteArray();
        }
    }

    /**
     * <p>Initializes a Datapoint class which packs up the passed data into messagepack byte arrays</p>
     *
     * @param data data to be packed
     * @throws AukletException to wrap any underlying exceptions.
     */
    public Datapoint(short data) throws AukletException {
        synchronized (this.msgpack) {
            this.msgpack.clear();
            try {
                this.msgpack.packMapHeader(1)
                        .packString(this.dataType).packShort(data);
            } catch (IOException e) {
                throw new AukletException("Could not assemble datapoint message", e);
            }
            this.dataValue = this.msgpack.toByteArray();
        }
    }

    /**
     * <p>Initializes a Datapoint class which packs up the passed data into messagepack byte arrays</p>
     *
     * @param data data to be packed
     * @throws AukletException to wrap any underlying exceptions.
     */
    public Datapoint(int data) throws AukletException {
        synchronized (this.msgpack) {
            this.msgpack.clear();
            try {
                this.msgpack.packMapHeader(1)
                        .packString(this.dataType).packInt(data);
            } catch (IOException e) {
                throw new AukletException("Could not assemble datapoint message", e);
            }
            this.dataValue = this.msgpack.toByteArray();
        }
    }

    /**
     * <p>Initializes a Datapoint class which packs up the passed data into messagepack byte arrays</p>
     *
     * @param data data to be packed
     * @throws AukletException to wrap any underlying exceptions.
     */
    public Datapoint(long data) throws AukletException {
        synchronized (this.msgpack) {
            this.msgpack.clear();
            try {
                this.msgpack.packMapHeader(1)
                        .packString(this.dataType).packLong(data);
            } catch (IOException e) {
                throw new AukletException("Could not assemble datapoint message", e);
            }
            this.dataValue = this.msgpack.toByteArray();
        }
    }

    /**
     * <p>Initializes a Datapoint class which packs up the passed data into messagepack byte arrays</p>
     *
     * @param data data to be packed
     * @throws AukletException to wrap any underlying exceptions.
     */
    public Datapoint(BigInteger data) throws AukletException {
        synchronized (this.msgpack) {
            this.msgpack.clear();
            try {
                this.msgpack.packMapHeader(1)
                        .packString(this.dataType).packBigInteger(data);
            } catch (IOException e) {
                throw new AukletException("Could not assemble datapoint message", e);
            }
            this.dataValue = this.msgpack.toByteArray();
        }
    }

    /**
     * <p>Initializes a Datapoint class which packs up the passed data into messagepack byte arrays</p>
     *
     * @param data data to be packed
     * @throws AukletException to wrap any underlying exceptions.
     */
    public Datapoint(float data) throws AukletException {
        synchronized (this.msgpack) {
            this.msgpack.clear();
            try {
                this.msgpack.packMapHeader(1)
                        .packString(this.dataType).packFloat(data);
            } catch (IOException e) {
                throw new AukletException("Could not assemble datapoint message", e);
            }
            this.dataValue = this.msgpack.toByteArray();
        }
    }

    /**
     * <p>Initializes a Datapoint class which packs up the passed data into messagepack byte arrays</p>
     *
     * @param data data to be packed
     * @throws AukletException to wrap any underlying exceptions.
     */
    public Datapoint(double data) throws AukletException {
        synchronized (this.msgpack) {
            this.msgpack.clear();
            try {
                this.msgpack.packMapHeader(1)
                        .packString(this.dataType).packDouble(data);
            } catch (IOException e) {
                throw new AukletException("Could not assemble datapoint message", e);
            }
            this.dataValue = this.msgpack.toByteArray();
        }
    }

    /**
     * <p>Initializes a Datapoint class which packs up the passed data into messagepack byte arrays</p>
     *
     * @param data data to be packed
     * @throws AukletException to wrap any underlying exceptions.
     */
    public Datapoint(byte[] data) throws AukletException {
        synchronized (this.msgpack) {
            this.msgpack.clear();
            try {
                this.msgpack.packMapHeader(1)
                        .packString(this.dataType).packBinaryHeader(data.length).addPayload(data);
            } catch (IOException e) {
                throw new AukletException("Could not assemble datapoint message", e);
            }
            this.dataValue = this.msgpack.toByteArray();
        }
    }

    /**
     * <p>Initializes a Datapoint class which packs up the passed data into messagepack byte arrays</p>
     *
     * @param data data to be packed
     * @throws AukletException to wrap any underlying exceptions.
     */
    public Datapoint(String data) throws AukletException {
        synchronized (this.msgpack) {
            this.msgpack.clear();
            try {
                this.msgpack.packMapHeader(1)
                        .packString(this.dataType).packString(data);
            } catch (IOException e) {
                throw new AukletException("Could not assemble datapoint message", e);
            }
            this.dataValue = this.msgpack.toByteArray();
        }
    }

    /**
     * <p>Initializes a Datapoint class which packs up the passed data into messagepack byte arrays</p>
     *
     * @param data data to be packed
     * @throws AukletException to wrap any underlying exceptions.
     */
    public Datapoint(List data) throws AukletException {
        synchronized (this.msgpack) {
            this.msgpack.clear();
            try {
                this.msgpack.packArrayHeader(1)
                        .packString(this.dataType).packArrayHeader(data.size());
            } catch (IOException e) {
                throw new AukletException("Could not assemble datapoint message", e);
            }
            this.dataValue = this.msgpack.toByteArray();
        }
    }

    /**
     * <p>Initializes a Datapoint class which packs up the passed data into messagepack byte arrays</p>
     *
     * @param data data to be packed
     * @throws AukletException to wrap any underlying exceptions.
     */
    public Datapoint(Map data) throws AukletException {
        synchronized (this.msgpack) {
            this.msgpack.clear();
            try {
                this.msgpack.packMapHeader(1)
                        .packString(this.dataType).packMapHeader(data.size());
            } catch (IOException e) {
                throw new AukletException("Could not assemble datapoint message", e);
            }
            this.dataValue = this.msgpack.toByteArray();
        }
    }
}