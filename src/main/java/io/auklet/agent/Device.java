package io.auklet.agent;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.util.Scanner;

public final class Device {

    private Device(){ }

    private static Logger logger = LoggerFactory.getLogger(Device.class);

    // appId is 22 bytes but AES is a 128-bit block cipher supporting keys of 128, 192, and 256 bits.
    private static final Key aesKey = new SecretKeySpec(Auklet.appId.substring(0,16).getBytes(), "AES");

    private static String clientId;
    private static String clientUsername;
    private static String clientPassword;
    private static String organization;

    public static boolean registerDevice(){
        String filename = "/.AukletAuth";

        try {
            Path fileLocation = Paths.get(Auklet.getFolderPath() + filename);
            byte[] data = Files.readAllBytes(fileLocation);
            logger.info("Auklet auth file content length: {}", data.length);

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            String decrypted = new String(cipher.doFinal(data));
            setCreds(new JSONObject(decrypted));

        } catch (FileNotFoundException | NoSuchFileException e) {
            logger.info("Creating a new Auklet auth file");
            JSONObject newObject = createDevice();
            if (newObject != null) {
                setCreds(newObject);
                writeCreds(Auklet.getFolderPath() + filename);
            } else return false;

        } catch (Exception e) {
            logger.error("Error during device registration", e);
            return false;
        }
        return true;
    }

    private static JSONObject createDevice() {
        HttpClient httpClient = HttpClientBuilder.create().build();

        try {
            JSONObject obj = new JSONObject();
            obj.put("mac_address_hash", Util.getMacAddressHash());
            obj.put("application", Auklet.appId);

            HttpPost request = new HttpPost(Auklet.getBaseUrl() + "/private/devices/");
            StringEntity params = new StringEntity(obj.toString());

            request.addHeader("content-type", "application/json");
            request.addHeader("Authorization", "JWT " + Auklet.apiKey);
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);

            String contents = Util.readContents(response);

            if(response.getStatusLine().getStatusCode() == 201 && contents != null) {
                return new JSONObject(contents);
            } else {
                logger.error("Error while creating device: {}: {}", response.getStatusLine(), contents);
            }
        } catch (Exception ex) {
            logger.error("Error while posting device info", ex);
        }
        return null;
    }

    private static void setCreds(JSONObject jsonObject) {
        clientPassword = jsonObject.getString("client_password");
        clientUsername = jsonObject.getString("id");
        clientId = jsonObject.getString("client_id");
        organization = jsonObject.getString("organization");
    }

    private static void writeCreds(String filename) {
        JSONObject obj = new JSONObject();
        obj.put("client_password", clientPassword);
        obj.put("id", clientUsername);
        obj.put("client_id", clientId);
        obj.put("organization", organization);

        try (FileOutputStream file = new FileOutputStream(filename)) {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            byte[] encrypted = cipher.doFinal(obj.toString().getBytes());

            file.write(encrypted);
            file.flush();

        } catch (Exception e) {
            logger.error("Error while writing Auklet auth credentials", e);
        }
    }

    public static String getClientUsername(){
        return clientUsername;
    }

    public static String getClientPassword(){
        return clientPassword;
    }

    public static String getClientId(){
        return clientId;
    }

    public static String getOrganization(){
        return organization;
    }

    public static boolean getCerts() {
        File caFile = new File(Auklet.getFolderPath() + "/CA");
        boolean success = false;
        if (caFile.exists()) {
            logger.info("CA file already exists");
            success = true;
        } else {
            try (FileWriter writer = new FileWriter(caFile)) {
                HttpClient httpClient = HttpClientBuilder.create().build();
                URL newUrl = new URL(Auklet.getBaseUrl() + "/private/devices/certificates/");
                HttpURLConnection con = (HttpURLConnection) newUrl.openConnection();

                con.setRequestProperty("Authorization", "JWT " + Auklet.apiKey);
                con.setDoInput(true);
                con.setRequestMethod("GET");
                con.setInstanceFollowRedirects(true);

                con.getResponseCode();

                HttpGet request = new HttpGet(con.getURL().toURI());
                HttpResponse response = httpClient.execute(request);

                String contents = Util.readContents(response);

                if (response.getStatusLine().getStatusCode() == 200 && contents != null) {
                    writer.write(contents);
                    logger.info("CA File has been created!");
                    success = true;
                } else {
                    logger.error("Error while getting certs: {}: {}",
                            response.getStatusLine(), contents);
                }
            } catch (Exception e) {
                logger.error("Error while getting CA cert", e);
            }
        }
        if (!success && Util.deleteFile(caFile)) {
            logger.info("CA file deleted");
        }
        return success;
    }

    public static boolean initLimitsConfig() {
        String limits = Auklet.folderPath + "/limits";
        try (FileWriter writer = new FileWriter(limits)) {
            DataRetention.setUsageFile(Auklet.getFolderPath() + "/usage");

            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(Auklet.getBaseUrl() + String.format("/private/devices/%s/app_config/",
                    Auklet.appId));
            request.addHeader("Authorization", "JWT " + Auklet.apiKey);
            HttpResponse response = httpClient.execute(request);
            if (logger.isInfoEnabled()) {
                logger.info(response.getStatusLine().toString());
            }

            if (response.getStatusLine().getStatusCode() == 200) {
                InputStream config = response.getEntity().getContent();
                String text;
                try (Scanner scanner = new Scanner(config, StandardCharsets.UTF_8.name())) {
                    text = scanner.useDelimiter("\\A").next();
                }
                JSONObject conf = new JSONObject(text).getJSONObject("config");

                writer.write(conf.toString());

                DataRetention.initDataRetention(conf);
                logger.info("Config File was stored");
            } else {
                logger.error("Get config response code: {}", response.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Unable to initialize Config", e);
            return false;
        }
        return true;
    }
}
