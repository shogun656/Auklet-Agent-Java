package io.auklet.agent;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AukletExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler defaultExceptionHandler;

    private AukletExceptionHandler(Thread.UncaughtExceptionHandler defaultExceptionHandler) {
        this.defaultExceptionHandler = defaultExceptionHandler;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable thrown) {

        if (defaultExceptionHandler != null) {
            // call the original handler
            defaultExceptionHandler.uncaughtException(thread, thrown);
        }

        else if (!(thrown instanceof ThreadDeath)) {
            sendEvent(thrown);
        }
    }

    protected static AukletExceptionHandler setup() {

        System.out.println("Configuring uncaught exception handler.");
        Thread.UncaughtExceptionHandler currentHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (currentHandler != null) {
            System.out.println("default UncaughtExceptionHandler class='" + currentHandler.getClass().getName() + "'");
        }

        AukletExceptionHandler handler = new AukletExceptionHandler(currentHandler);
        Thread.setDefaultUncaughtExceptionHandler(handler);
        return handler;
    }

    protected static synchronized void sendEvent(Throwable thrown) {
        List<Object> list = new ArrayList<>();
        System.err.print("Exception in thread \"" + Thread.currentThread().getName() + "\" ");
        thrown.printStackTrace(System.err);

        System.out.println("Exception message from app  " + thrown.getMessage());

        for (StackTraceElement se : thrown.getStackTrace()) {
            Map<String, Object> map = new HashMap<>();
            map.put("functionName", se.getMethodName());
            map.put("className", se.getClassName());
            map.put("filePath", se.getFileName());
            map.put("lineNumber", se.getLineNumber());
            list.add(map);
        }
        setStackTrace(list, thrown.toString());

        try {
            byte[] bytesToSend = Messages.createMessagePack();
            if (!DataRetention.hasExceededDataLimit(bytesToSend.length)) {
                MqttMessage message = new MqttMessage(bytesToSend);
                message.setQos(1); // At Least Once Semantics
                Auklet.client.publish("java/events/" + Device.getOrganization() + "/" +
                        Device.getClient_Username(), message);
                System.out.println("Message published");
            }
        } catch (MqttException | NullPointerException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    private static void setStackTrace(List<Object> stackTraceList, String exceptionMessage){
        Messages.map.put("stackTrace", stackTraceList);
        Messages.map.put("timestamp", System.currentTimeMillis());
        Messages.map.put("excType", exceptionMessage);

    }

}
