package io.auklet;

import java.math.BigInteger;
import io.auklet.AukletException;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;

import java.io.*;
import java.util.*;


public class Datapoint {

    @GuardedBy("itself") protected final MessageBufferPacker msgpack = MessagePack.newDefaultBufferPacker();
    private final String dataType = "value";
    public byte[] dataValue;

    /**
     * <p>This method returns a the byte array matching a map whose key is "value"</p>
     *
     * @param data
     */
    public Datapoint(boolean data) {
        synchronized (this.msgpack) {
            this.msgpack.clear();
            try {
                this.msgpack.packMapHeader(1)
                        .packString(this.dataType).packBoolean(data);
            } catch (IOException e) {
                throw new AukletException("Could not assemble datapoint message", e);
            }
            this.datavalue = this.msgpack.toByteArray();
        }
    }

    /**
     * <p>This method returns a the byte array matching a map whose key is "value"</p>
     *
     * @param data
     */
    public Datapoint(byte data) {
        synchronized (this.msgpack) {
            this.msgpack.clear();
            try {
                this.msgpack.packMapHeader(1)
                        .packString(this.dataType).packByte(data);
            } catch (IOException e) {
                throw new AukletException("Could not assemble datapoint message", e);
            }
            this.datavalue = this.msgpack.toByteArray();
        }
    }

    /**
     * <p>This method returns a the byte array matching a map whose key is "value"</p>
     *
     * @param data
     */
    public Datapoint(short data) {
        synchronized (this.msgpack) {
            this.msgpack.clear();
            try {
                this.msgpack.packMapHeader(1)
                        .packString(this.dataType).packShort(data);
            } catch (IOException e) {
                throw new AukletException("Could not assemble datapoint message", e);
            }
            this.datavalue = this.msgpack.toByteArray();
        }
    }

    /**
     * <p>This method returns a the byte array matching a map whose key is "value"</p>
     *
     * @param data
     */
    public Datapoint(int data) {
        synchronized (this.msgpack) {
            this.msgpack.clear();
            try {
                this.msgpack.packMapHeader(1)
                        .packString(this.dataType).packInt(data);
            } catch (IOException e) {
                throw new AukletException("Could not assemble datapoint message", e);
            }
            this.datavalue = this.msgpack.toByteArray();
        }
    }

    /**
     * <p>This method returns a the byte array matching a map whose key is "value"</p>
     *
     * @param data
     */
    public Datapoint(long data) {
        synchronized (this.msgpack) {
            this.msgpack.clear();
            try {
                this.msgpack.packMapHeader(1)
                        .packString(this.dataType).packLong(data);
            } catch (IOException e) {
                throw new AukletException("Could not assemble datapoint message", e);
            }
            this.datavalue = this.msgpack.toByteArray();
        }
    }

    /**
     * <p>This method returns a the byte array matching a map whose key is "value"</p>
     *
     * @param data
     */
    public Datapoint(BigInteger data) {
        synchronized (this.msgpack) {
            this.msgpack.clear();
            try {
                this.msgpack.packMapHeader(1)
                        .packString(this.dataType).packBigInteger(data);
            } catch (IOException e) {
                throw new AukletException("Could not assemble datapoint message", e);
            }
            this.datavalue = this.msgpack.toByteArray();
        }
    }

    /**
     * <p>This method returns a the byte array matching a map whose key is "value"</p>
     *
     * @param data
     */
    public Datapoint(float data) {
        synchronized (this.msgpack) {
            this.msgpack.clear();
            try {
                this.msgpack.packMapHeader(1)
                        .packString(this.dataType).packFloat(data);
            } catch (IOException e) {
                throw new AukletException("Could not assemble datapoint message", e);
            }
            this.datavalue = this.msgpack.toByteArray();
        }
    }

    /**
     * <p>This method returns a the byte array matching a map whose key is "value"</p>
     *
     * @param data
     */
    public Datapoint(double data) {
        synchronized (this.msgpack) {
            this.msgpack.clear();
            try {
                this.msgpack.packMapHeader(1)
                        .packString(this.dataType).packDouble(data);
            } catch (IOException e) {
                throw new AukletException("Could not assemble datapoint message", e);
            }
            this.datavalue = this.msgpack.toByteArray();
        }
    }

    /**
     * <p>This method returns a the byte array matching a map whose key is "value"</p>
     *
     * @param data
     */
    public Datapoint(byte[] data) {
        synchronized (this.msgpack) {
            this.msgpack.clear();
            try {
                this.msgpack.packMapHeader(1)
                        .packString(this.dataType).packBinaryHeader(data.length).addPaylod(data);
            } catch (IOException e) {
                throw new AukletException("Could not assemble datapoint message", e);
            }
            this.datavalue = this.msgpack.toByteArray();
        }
    }

    /**
     * <p>This method returns a the byte array matching a map whose key is "value"</p>
     *
     * @param data
     */
    public Datapoint(String data) {
        synchronized (this.msgpack) {
            this.msgpack.clear();
            try {
                this.msgpack.packMapHeader(1)
                        .packString(this.dataType).packString(data);
            } catch (IOException e) {
                throw new AukletException("Could not assemble datapoint message", e);
            }
            this.datavalue = this.msgpack.toByteArray();
        }
    }

    /**
     * <p>This method returns a the byte array matching a map whose key is "value"</p>
     *
     * @param data
     */
    public Datapoint(List data) {
        synchronized (this.msgpack) {
            this.msgpack.clear();
            try {
                this.msgpack.packArrayHeader(1)
                        .packString(this.dataType).packArrayHeader(data.size());
            } catch (IOException e) {
                throw new AukletException("Could not assemble datapoint message", e);
            }
            this.datavalue = this.msgpack.toByteArray();
        }
    }

    /**
     * <p>This method returns a the byte array matching a map whose key is "value"</p>
     *
     * @param data
     */
    public Datapoint(Map data) {
        synchronized (this.msgpack) {
            this.msgpack.clear();
            try {
                this.msgpack.packMapHeader(1)
                        .packString(this.dataType).packMapHeader(data.size());
            } catch (IOException e) {
                throw new AukletException("Could not assemble datapoint message", e);
            }
            this.datavalue = this.msgpack.toByteArray();
        }
    }
}