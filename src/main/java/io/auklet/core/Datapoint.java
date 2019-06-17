package io.auklet.core;

import java.math.BigInteger;
import java.math.BigDecimal;
import io.auklet.AukletException;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.value.Value;
import org.msgpack.value.ValueFactory;
import net.jcip.annotations.GuardedBy;

import java.io.*;
import java.util.*;


public final class Datapoint {

    private static String SIMPLE_KEY = "value";
    private final Value value;

    /**
     * <p>Initializes a Datapoint class which packs up the passed data into messagepack byte arrays</p>
     *
     * @param data data to be packed
     * @throws AukletException to wrap any underlying exceptions.
     */
    public Datapoint(Object data) throws AukletException {

        MessageBufferPacker msgpack = MessagePack.newDefaultBufferPacker();
        msgpack.clear();
        try {
            if (data == null) {
                this.value = ValueFactory.newMap(
                    ValueFactory.newString(this.SIMPLE_KEY),
                    ValueFactory.nilValue()
                );
            } else if (data.isPrimitive()) {
                String className = data.getClass().getSimpleName();
                switch (className) {
                    case "Integer":
                        this.value = ValueFactory.newMap(
                            ValueFactory.newString(this.SIMPLE_KEY),
                            ValueFactory.newInteger((int) data)
                        );
                        break;
                    case "Float":
                        this.value = ValueFactory.newMap(
                            ValueFactory.newString(this.SIMPLE_KEY),
                            ValueFactory.newFloat((float) data)
                        );
                        break;
                    case "Short":
                        this.value = ValueFactory.newMap(
                            ValueFactory.newString(this.SIMPLE_KEY),
                            ValueFactory.newShort((short) data)
                        );
                        break;
                    case "Long":
                        this.value = ValueFactory.newMap(
                            ValueFactory.newString(this.SIMPLE_KEY),
                            ValueFactory.newLong((long) data)
                        );
                        break;
                    case "Double":
                        this.value = ValueFactory.newMap(
                            ValueFactory.newString(this.SIMPLE_KEY),
                            ValueFactory.newDouble((double) data)
                        );
                        break;
                    case "Byte":
                        this.value = ValueFactory.newMap(
                            ValueFactory.newString(this.SIMPLE_KEY),
                            ValueFactory.newByte((byte) data)
                        );
                        break;
                    case "Boolean":
                        this.value = ValueFactory.newMap(
                            ValueFactory.newString(this.SIMPLE_KEY),
                            ValueFactory.newBoolean((boolean) data)
                        );
                        break;
                }
            } else if (data.getClass().isArray()) {
                this.value = ValueFactory.newMap(
                    ValueFactory.newString(this.SIMPLE_KEY),
                    ValueFactory.newArray((Array) data)
                );
            } else if (data instanceof List) {
                this.value = ValueFactory.newMap(
                    ValueFactory.newString(this.SIMPLE_KEY),
                    ValueFactory.newArrayFrom((List) data)
                );
            } else if (data instanceof Map) {
                this.value = ValueFactory.newMap(
                    ValueFactory.newString(this.SIMPLE_KEY),
                    ValueFactory.newMap((Map) data)
                );
            } else if (data instanceof String) {
                this.value = ValueFactory.newMap(
                    ValueFactory.newString(this.SIMPLE_KEY),
                    ValueFactory.newString((String) data)
                );
            } else if (data instanceof BigInteger) {
                BigInteger newInt = (BigInteger) data;
                this.value = ValueFactory.newMap(
                    ValueFactory.newString(this.SIMPLE_KEY),
                    ValueFactory.newString(newInt.toString())
                );
            } else if (data instanceof BigDecimal) {
                BigDecimal newDec = (BigDecimal) data;
                this.value = ValueFactory.newMap(
                    ValueFactory.newString(this.SIMPLE_KEY),
                    ValueFactory.newString(newDec.toString())
                );
            } else if (data instanceof Datapoint) {
                Datapoint newDatapoint = (Datapoint) data;
                this.value = newDatapoint.getValue();
            }
        } catch (IOException e) {
            throw new AukletException("Could not assemble datapoint message", e);
        }
    }

    public Value getValue() {
        Value copied;
        return copied = this.value;
    }
}