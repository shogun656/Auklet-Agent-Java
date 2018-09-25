package io.auklet.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Messages {

    public static Map<String, Object> map = new HashMap<>();

    public static byte[] createMessagePack(){
        byte[] bytes = new byte[]{};
        try {
            map.put("id", UUID.randomUUID());
            map.put("application", Auklet.AppId);
            map.put("macAddresshash", util.getMacAddressHash());
            map.put("publicIP", util.getIpAddress());

            System.out.println("message pack: " + map);

            ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory());
            bytes = objectMapper.writeValueAsBytes(map);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return bytes;
    }
}
