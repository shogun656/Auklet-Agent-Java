package io.auklet.core;

import java.math.BigInteger;
import java.math.BigDecimal;
import io.auklet.AukletException;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.value.Value;
import org.msgpack.value.ImmutableValue;
import org.msgpack.value.ValueFactory;
import net.jcip.annotations.Immutable;

import java.io.*;
import java.util.*;

/**
 *
 */
@Immutable
public final class Datapoint {

    private static String SIMPLE_KEY = "value";
    private final ImmutableValue value;

    /**
     * <p>Initializes a Datapoint class which packs up the passed data into messagepack byte arrays</p>
     *
     * @param data data to be packed
     * @throws AukletException to wrap any underlying exceptions.
     */
    public Datapoint(@Nullable Object data) throws AukletException {

        MessageBufferPacker msgpack = MessagePack.newDefaultBufferPacker();
        msgpack.clear();
        if (data == null) {
            this.value = ValueFactory.newMap(
                ValueFactory.newString(this.SIMPLE_KEY),
                ValueFactory.newNil()
            );
        } else if (data.getClass().isPrimitive()) {
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
                        ValueFactory.newInteger((short) data)
                    );
                    break;
                case "Long":
                    this.value = ValueFactory.newMap(
                        ValueFactory.newString(this.SIMPLE_KEY),
                        ValueFactory.newInteger((long) data)
                    );
                    break;
                case "Double":
                    this.value = ValueFactory.newMap(
                        ValueFactory.newString(this.SIMPLE_KEY),
                        ValueFactory.newFloat((double) data)
                    );
                    break;
                case "Byte":
                    this.value = ValueFactory.newMap(
                        ValueFactory.newString(this.SIMPLE_KEY),
                        ValueFactory.newInteger((byte) data)
                    );
                    break;
                case "Boolean":
                    this.value = ValueFactory.newMap(
                        ValueFactory.newString(this.SIMPLE_KEY),
                        ValueFactory.newBoolean((boolean) data)
                    );
                    break;
                default:
                    // This is impossible
                    throw new AukletException("Error constructing Datapoint");
            }
        } else if (data.getClass().isArray()) {
            // construct new array of values from looping through all members
            List<ImmutableValue> newDatapoints = new ArrayList<ImmutableValue>();
            Object[] objects = (Object[]) data;
            for (int i = 0; i < objects.length; i++) {
                Datapoint newData = new Datapoint(objects[i]);
                newDatapoints.add(newData.getValue());
            }
            this.value = ValueFactory.newMap(
                ValueFactory.newString(this.SIMPLE_KEY),
                ValueFactory.newArray(newDatapoints)
            );
        } else if (data instanceof List) {
            // construct new array of values from looping through all members
            List<ImmutableValue> newDatapoints = new ArrayList<ImmutableValue>();
            List objects = (List) data;
            for (Object object : objects) {
                Datapoint newData = new Datapoint(object);
                newDatapoints.add(newData.getValue());
            }
            this.value = ValueFactory.newMap(
                ValueFactory.newString(this.SIMPLE_KEY),
                ValueFactory.newArray(newDatapoints)
            );
        } else if (data instanceof Map) {
            Map<ImmutableValue, ImmutableValue> valueMap = new HashMap<ImmutableValue, ImmutableValue>();
            Map<Object, Object> castedMap = (Map) data;
            for (Map.Entry<Object, Object> entry : castedMap.entrySet()) {
                Datapoint key = new Datapoint(entry.getKey());
                Datapoint value = new Datapoint(entry.getValue());
                valueMap.put(key.getValue(), value.getValue());
            }
            this.value = ValueFactory.newMap(
                ValueFactory.newString(this.SIMPLE_KEY),
                ValueFactory.newMap(valueMap)
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
        } else {
            throw new AukletException("We do not currently support the " + data.getClass().getName() + " class you have submitted");
        }
    }

    /**
     * <p>Returns the value converted to an ImmutableValue</p>
     *
     * @return The ImmutableValue object stored in this datapoint
     */
    @NonNull public ImmutableValue getValue() {
        return this.value;
    }

    /**
     * <p>Currently not utilized, will evnetually return a string representation of the datapoint for logging purposes</p>
     *
     * @return Empty string
     */
    public String toString() {
        return "";
    }
}