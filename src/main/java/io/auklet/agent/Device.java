package io.auklet.agent;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.*;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import java.security.*;

public class Device {

    private static String filename;
    private static String client_id;
    private static String client_username;
    private static String client_password;
    private static String organization;

    public static void register_device(String deviceId){

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
            obj.put("mac_address_has", getMacAddress());
            obj.put("application", Auklet.AppId);
            HttpPost request = new HttpPost(Auklet.baseUrl + "/devices/");
            //StringEntity params =new StringEntity("details={\"name\":\"myname\",\"age\":\"20\"} ");
            StringEntity params = new StringEntity(obj.toJSONString());
            //request.addHeader("content-type", "application/json");
            request.addHeader("Authorization", "JWT"+Auklet.ApiKey);
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);
            if(response.getStatusLine().getStatusCode() != 200){
                System.out.println("could not create a device and status code is: " + response.getStatusLine().getStatusCode());
                throw new Exception();
            }
            JSONParser parser = new JSONParser();
            JSONObject myResponse = (JSONObject) parser.parse(response.toString());
            return myResponse;

            //handle response here...

        }catch (Exception ex) {

            //handle exception here
        }
        return null;
    }

    private static String getMacAddress() {
        InetAddress ip;
        String machash = "";
        try {

            ip = InetAddress.getLocalHost();
            System.out.println("Current IP address : " + ip.getHostAddress());

            NetworkInterface network = NetworkInterface.getByInetAddress(ip);

            byte[] mac = network.getHardwareAddress();

            System.out.print("Current MAC address : ");

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            System.out.println(sb.toString());

            byte[] macBytes = String.valueOf(sb).getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] macHashByte = md.digest(macBytes);
            machash = Hex.encodeHexString(macHashByte);


        } catch (UnknownHostException | SocketException | NoSuchAlgorithmException | UnsupportedEncodingException e) {

            e.printStackTrace();

        }
        return machash;

    }

    private static void getCreds(JSONObject jsonObject) {

        System.out.println(jsonObject);

        client_password = (String) jsonObject.get("client_password");
        System.out.println(client_password);

        client_username = (String) jsonObject.get("id");
        System.out.println(client_username);

        client_id = (String) jsonObject.get("client_id");
        System.out.println(client_id);

        organization = (String) jsonObject.get("organization");
        System.out.println(organization);

        //if (pass.equals("") | id.equals("") | client_id.equals("") | organization.equals("")){
        //    JSONObject newObject = create_device();
        //}
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
