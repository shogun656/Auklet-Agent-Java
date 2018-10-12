package io.auklet.agent;

import org.apache.commons.codec.binary.Hex;

import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public final class Util {

    private Util(){ }

    protected static String getMacAddressHash() {
        InetAddress ip;
        String machash = "";
        NetworkInterface networkinterface = null;
        try {

            System.out.println("Host addr: " + InetAddress.getLocalHost().getHostAddress());  // often returns "127.0.0.1"
            Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
            for (; n.hasMoreElements();)
            {
                NetworkInterface e = n.nextElement();
                System.out.println("Interface: " + e.getName());
                if (!e.isLoopback()) {
                    networkinterface = e;
                }
                break;
            }

            byte[] mac = networkinterface.getHardwareAddress();

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            System.out.println("Current MAC address : " + sb.toString());

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
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));

            ipAddr = in.readLine(); //you get the IP as a String
            System.out.println("Current IP Address: " + ipAddr);

        } catch (IOException e) {
            e.printStackTrace();

        }
        return ipAddr;
    }

    protected static Map<String, Object> getSystemMetrics(){
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

    protected static String createCustomFolder(String sysProperty) {

        String path = System.getProperty(sysProperty) + File.separator + "aukletFiles";
        File newfile = new File(path);
        if (newfile.exists()){
            System.out.println("folder already exists");
        } else if (newfile.mkdir()){
            System.out.println("folder created");
        } else {
            System.out.println("folder was not created for " + sysProperty);
            return null;
        }

        return path;
    }
}
