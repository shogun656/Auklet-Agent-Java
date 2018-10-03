package io.auklet.agent;

import org.apache.commons.codec.binary.Hex;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class util {

    protected static String getMacAddressHash() {
        InetAddress ip;
        String machash = "";
        try {

            ip = InetAddress.getLocalHost();
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

    protected static String getIpAddress(){
        String ipAddr = "";
        try {
            InetAddress ip = InetAddress.getLocalHost();
            System.out.println("Current IP address : " + ip.getHostAddress());
            ipAddr = ip.getHostAddress();

        } catch (UnknownHostException e) {
            e.printStackTrace();

        }
        return ipAddr;
    }

    public static Map<String, Object> getSystemMetrics(){
        try {
            Map<String, Object> obj = new HashMap<>();
            obj.put("outboundNetwork", 0);
            obj.put("inboundNetwork", 0);
            obj.put("memoryUsage", 0);
            obj.put("cpuUsage", 0.0);

            return obj;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return null;
    }

    protected static String createCustomFolder() {

        String path = System.getProperty("user.home") + File.separator + "aukletFiles";
        File newfile = new File(path);
        if (newfile.exists()){
            System.out.println("folder already exists");
        } else if (newfile.mkdir()){
            System.out.println("folder created");
        } else {
            System.out.println("folder was not created");
            path = System.getProperty("java.io.tmpdir") + File.separator + "aukletFiles";
            newfile = new File(path);
            newfile.mkdir();
        }

        return path;
    }
}
