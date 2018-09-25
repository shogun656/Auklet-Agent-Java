package io.auklet.agent;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.*;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Device {

    private static String filename = "./AukletAuth";
    private static String client_id;
    private static String client_username;
    private static String client_password;
    private static String organization;

    public static void register_device(){

        JSONParser parser = new JSONParser();

        try {
            Object obj = parser.parse(new FileReader(filename));
            JSONObject jsonObject = (JSONObject) obj;
            getCreds(jsonObject);

        } catch (FileNotFoundException e) {
            JSONObject newObject = create_device();
            getCreds(newObject);
            writeCreds(filename);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private static JSONObject create_device(){
        HttpClient httpClient = HttpClientBuilder.create().build();

        try {
            JSONObject obj = new JSONObject();
            obj.put("mac_address_hash", util.getMacAddressHash());
            obj.put("application", Auklet.AppId);
            HttpPost request = new HttpPost(Auklet.baseUrl + "/private/devices/");
            StringEntity params = new StringEntity(obj.toJSONString());
            request.addHeader("content-type", "application/json");
            request.addHeader("Authorization", "JWT "+Auklet.ApiKey);
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);
            System.out.println(response.getEntity().getContent());

            String text = null;
            try (Scanner scanner = new Scanner(response.getEntity().getContent(), StandardCharsets.UTF_8.name())) {
                text = scanner.useDelimiter("\\A").next();
            }

            System.out.println("content is: " + text);
            if(response.getStatusLine().getStatusCode() != 201){
                System.out.println("could not create a device and status code is: " + response.getStatusLine().getStatusCode());
                throw new Exception();
            }
            JSONParser parser = new JSONParser();
            JSONObject myResponse = (JSONObject) parser.parse(text);
            System.out.println(myResponse.toJSONString());
            return myResponse;

            //handle response here...

        }catch (Exception ex) {

            //handle exception here
        }
        return null;
    }

    private static void getCreds(JSONObject jsonObject) {

        System.out.println(jsonObject);

        client_password = (String) jsonObject.get("client_password");
        System.out.println(client_password);

        client_username = (String) jsonObject.get("client_username");
        System.out.println(client_username);

        client_id = (String) jsonObject.get("client_id");
        System.out.println(client_id);

        organization = (String) jsonObject.get("organization");
        System.out.println(organization);
    }

    private static void writeCreds(String filename){

        JSONObject obj = new JSONObject();
        obj.put("client_password", client_password);
        obj.put("client_username", client_username);
        obj.put("client_id", client_id);
        obj.put("organization", organization);

        try (FileWriter file = new FileWriter(filename)) {

            file.write(obj.toJSONString());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
