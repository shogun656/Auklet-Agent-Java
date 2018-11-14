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
        } else if (!(thrown instanceof ThreadDeath)) {
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
        Map<String, Object> map = startMessage(list, thrown.toString());
        byte[] bytesToSend = Messages.createMessagePack(map);

        Auklet.client.sendEvent(bytesToSend);
    }

    private static Map<String, Object> startMessage(List<Object> stackTraceList, String exceptionMessage) {
        Map<String, Object> map = new HashMap<>();
        map.put("stackTrace", stackTraceList);
        map.put("timestamp", System.currentTimeMillis());
        map.put("excType", exceptionMessage);
        return map;
    }

}
