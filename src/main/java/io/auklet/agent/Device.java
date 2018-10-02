package io.auklet.agent;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.PEMParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.*;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
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
            setCreds(jsonObject);

        } catch (FileNotFoundException e) {
            JSONObject newObject = create_device();
            setCreds(newObject);
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

    private static void setCreds(JSONObject jsonObject) {

        System.out.println(jsonObject);

        client_password = (String) jsonObject.get("client_password");
        System.out.println(client_password);

        client_username = (String) jsonObject.get("id");
        System.out.println(client_username);

        client_id = (String) jsonObject.get("client_id");
        System.out.println(client_id);

        organization = (String) jsonObject.get("organization");
        System.out.println(organization);
    }

    private static void writeCreds(String filename){

        JSONObject obj = new JSONObject();
        obj.put("client_password", client_password);
        obj.put("id", client_username);
        obj.put("client_id", client_id);
        obj.put("organization", organization);

        try (FileWriter file = new FileWriter(filename)) {

            file.write(obj.toJSONString());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String getClient_username(){
        return client_username;
    }

    public static String getClient_password(){
        return client_password;
    }

    public static String getClient_id(){
        return client_id;
    }

    public static String getOrganization(){
        return organization;
    }

    public static void get_certs() {


        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            URL newUrl = new URL(Auklet.baseUrl + "private/devices/certificates/");
            HttpURLConnection con = (HttpURLConnection) newUrl.openConnection();

            con.setRequestProperty("Authorization", "JWT " + Auklet.ApiKey);;
            con.setDoInput(true);
            con.setRequestMethod("GET");
            con.setInstanceFollowRedirects(true);

            System.out.println("redirect url: " + con.getURL().toURI());
            con.getResponseCode();


            System.out.println("redirect url after redirect: " + con.getURL().toURI());
            HttpGet request = new HttpGet(con.getURL().toURI());
            HttpResponse response = httpClient.execute(request);
            InputStream ca = response.getEntity().getContent();
            String text = null;
            try (Scanner scanner = new Scanner(ca, StandardCharsets.UTF_8.name())) {
                text = scanner.useDelimiter("\\A").next();
            }
            System.out.println("ca content is: " + text);
            File file = new File("./CA");
            if (file.createNewFile())
            {
                System.out.println("File is created!");
                BufferedWriter writer = new BufferedWriter(new FileWriter("./CA"));
                writer.write(text);
                writer.close();

            } else {
                System.out.println("File already exists.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

    }
}
