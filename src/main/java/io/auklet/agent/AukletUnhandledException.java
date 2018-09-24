package io.auklet.agent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.template.ObjectArrayTemplate;
import org.msgpack.template.Template;
import org.msgpack.template.Templates;
import org.msgpack.unpacker.Converter;
import org.msgpack.value.StringValue;
import org.msgpack.value.Value;
//import org.msgpack.MessagePack;

public class AukletUnhandledException implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler defaultExceptionHandler;
    private List<Object> stackTrace;

    public AukletUnhandledException(Thread.UncaughtExceptionHandler defaultExceptionHandler) {
        this.defaultExceptionHandler = defaultExceptionHandler;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable thrown) {

        if (defaultExceptionHandler != null) {
            // call the original handler
            defaultExceptionHandler.uncaughtException(thread, thrown);
            System.out.println("We are here");
        }

        else if (!(thrown instanceof ThreadDeath)) {
            List<Object> list = new ArrayList<>();
            //MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
            // CHECKSTYLE.OFF: RegexpSinglelineJava
            System.err.print("Exception in thread \"" + thread.getName() + "\" ");
            thrown.printStackTrace(System.err);

            System.out.println("library Uncaught Exception caught from app  " + thrown.getMessage());
            System.out.println("Uncaught Exception stacktrace is ");
            for (StackTraceElement se : thrown.getStackTrace()) {
                Map<String, Object> map = new HashMap<>();
                map.put("functionName", se.getMethodName());
                map.put("className", se.getClassName());
                map.put("filePath", se.getFileName());
                map.put("lineNumber", se.getLineNumber());
                list.add(map);
                System.out.println(list);
            }
            setStackTrace(list);
            createMessagePack();
        }


    }

    public static AukletUnhandledException setup() {

        System.out.println("Configuring uncaught exception handler.");
        Thread.UncaughtExceptionHandler currentHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (currentHandler != null) {
            System.out.println("default UncaughtExceptionHandler class='" + currentHandler.getClass().getName() + "'");
        }

        AukletUnhandledException handler = new AukletUnhandledException(currentHandler);
        Thread.setDefaultUncaughtExceptionHandler(handler);
        return handler;
    }

    private void setStackTrace(List<Object> stackTrace){
        this.stackTrace = stackTrace;
    }

    private List<Object> getStacktrace(){
        return this.stackTrace;
    }

    private void createMessagePack(){
        try {
            System.out.println(getStacktrace());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(getStacktrace());
            out.flush();
            byte[] yourBytes = bos.toByteArray();
            System.out.println(yourBytes.length);

            packer.packBinaryHeader(yourBytes.length);
            packer.addPayload(yourBytes);
            //packer.writePayload(yourBytes);

            packer.close();

            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(packer.toByteArray());
            byte[] readData = new byte[]{};
            unpacker.readPayload(readData);
            System.out.println(new String(packer.toByteArray()));
            //Value v = unpacker.unpackValue();
            //StringValue readData = v.asStringValue();
            //System.out.println(readData.length);
            //String s = new String(readData);
            System.out.println("Reading message Pack: " + new String(readData));
        } catch (Exception e){
            System.out.println("Some Exception");
            e.printStackTrace();
        }

    }

    /*
    private void createMessagePack1(){
        try {
            Map<Object, Object> src = new HashMap<>();
            src.put("stacktrace", getStacktrace());
            src.put("ip", "1.0.2.0");
            src.put("exception", "exit");

            //ByteArrayOutputStream bos = new ByteArrayOutputStream();
            //ObjectOutputStream out = new ObjectOutputStream(bos);
            //out.writeObject(getStacktrace());
            //out.flush();
            //byte[] yourBytes = bos.toByteArray();

            MessagePack msgpack = new MessagePack();
            // Serialize
            byte[] raw = msgpack.write(src);

            // Deserialize directly using a template
            //byte[] dst = msgpack.read(raw, Templates.TByteArray);
            //Map<String, Object> dst = msgpack.read(raw, Templates.tMap(Templates.TString, Templates.TValue))
            //List<String> dst1 = msgpack.read(raw, Templates.tList(Templates.TString));
            //System.out.println(dst1.get(0));
            //System.out.println(dst1.get(1));
            //System.out.println(new String(dst));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    */
}
