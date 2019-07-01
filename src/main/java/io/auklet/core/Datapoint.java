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
 * <p>Datapoints are a way to log your own custom data to the auklet backend.</p>
 */
@Immutable
public final class Datapoint {

    private static String simpleKey = "value";
    private final ImmutableValue value;
    private final String asString;

    /**
     * <p>Initializes a Datapoint class which packs up the passed data into messagepack byte arrays</p>
     *
     * @param data data to be packed
     * @throws AukletException to wrap any underlying exceptions.
     */
    public Datapoint(@Nullable Object data) throws AukletException {
        if (data instanceof Datapoint) {
            Datapoint newDatapoint = (Datapoint) data;
            this.value = newDatapoint.getValue();
            this.asString = newDatapoint.toString();
        } else {
            Map.Entry<String, Value> converted = Datapoint.convertData(data).entrySet().iterator().next();
            this.value = ValueFactory.newMap(
                    ValueFactory.newString(Datapoint.simpleKey),
                    converted.getValue()
            );
            this.asString = converted.getKey();
        }
    }

    @NonNull private static Map<String, Value> convertData(@Nullable Object data) throws AukletException {
        Map<String, Value> valueMap = new HashMap<>();
        if (data == null) {
            valueMap.put("null", ValueFactory.newNil());
        } else if (data.getClass().isPrimitive()) {
            String className = data.getClass().getSimpleName();
            switch (className) {
                case "Integer":
                    valueMap.put(String.valueOf((int) data),
                            ValueFactory.newInteger((int) data));
                    break;
                case "Float":
                    valueMap.put(String.valueOf((float) data),
                            ValueFactory.newFloat((float) data));
                    break;
                case "Short":
                    valueMap.put(String.valueOf((short) data),
                            ValueFactory.newInteger((short) data));
                    break;
                case "Long":
                    valueMap.put(String.valueOf((long) data),
                            ValueFactory.newInteger((long) data));
                    break;
                case "Double":
                    valueMap.put(String.valueOf((double) data),
                            ValueFactory.newFloat((double) data));
                    break;
                case "Byte":
                    valueMap.put(String.valueOf((byte) data),
                            ValueFactory.newInteger((byte) data));
                    break;
                case "Boolean":
                    valueMap.put(String.valueOf((boolean) data),
                            ValueFactory.newBoolean((boolean) data));
                    break;
                default:
                    // This is impossible
                    throw new AukletException("Error constructing Datapoint");
            }
        } else if (data instanceof Integer) {
            valueMap.put(Integer.toString((Integer) data),
                    ValueFactory.newInteger((int) data));
        } else if (data instanceof Float) {
            valueMap.put(Float.toString((Float) data),
                    ValueFactory.newFloat((float) data));
        } else if (data instanceof Short) {
            valueMap.put(Short.toString((Short) data),
                    ValueFactory.newFloat((short) data));
        } else if (data instanceof Long) {
            valueMap.put(Long.toString((Long) data),
                    ValueFactory.newInteger((long) data));
        } else if (data instanceof Double) {
            valueMap.put(Double.toString((Double) data),
                    ValueFactory.newFloat((double) data));
        } else if (data instanceof Byte) {
            valueMap.put(Byte.toString((Byte) data),
                    ValueFactory.newInteger((byte) data));
        } else if (data instanceof Boolean) {
            valueMap.put(Boolean.toString((Boolean) data),
                    ValueFactory.newBoolean((boolean) data));
        } else if (data.getClass().isArray() || data instanceof List) {
            // construct new array of values from looping through all members
            List<ImmutableValue> newDatapoints = new ArrayList<>();
            Object[] objects = (Object[]) data;
            for (int i = 0; i < objects.length; i++) {
                Datapoint newData = new Datapoint(objects[i]);
                newDatapoints.add(newData.getValue());
            }
            valueMap.put(
                    newDatapoints.toString(),
                    ValueFactory.newArray(newDatapoints)
            );
        } else if (data instanceof Map) {
            Map<ImmutableValue, ImmutableValue> convertedMap = new HashMap<>();
            Map<Object, Object> castedMap = (Map) data;
            for (Map.Entry<Object, Object> entry : castedMap.entrySet()) {
                Datapoint key = new Datapoint(entry.getKey());
                Datapoint newValue = new Datapoint(entry.getValue());
                convertedMap.put(key.getValue(), newValue.getValue());
            }
            valueMap.put(
                    convertedMap.toString(), ValueFactory.newMap(convertedMap)
            );
        } else if (data instanceof String) {
            valueMap.put((String) data, ValueFactory.newString((String) data));
        } else if (data instanceof BigInteger) {
            BigInteger newInt = (BigInteger) data;
            valueMap.put(newInt.toString(), ValueFactory.newString(newInt.toString()));
        } else if (data instanceof BigDecimal) {
            BigDecimal newDec = (BigDecimal) data;
            valueMap.put(newDec.toString(), ValueFactory.newString(newDec.toString()));
        } else {
            throw new AukletException("We do not currently support the " + data.getClass().getName() + " class you have submitted");
        }
        return valueMap;
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
        return asString;
    }
}