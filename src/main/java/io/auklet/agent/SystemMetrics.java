package io.auklet.agent;

import com.sun.management.OperatingSystemMXBean;
import org.apache.commons.lang3.SystemUtils;
import oshi.hardware.NetworkIF;
import oshi.hardware.common.AbstractGlobalMemory;
import oshi.hardware.common.AbstractNetworks;
import oshi.hardware.platform.linux.LinuxGlobalMemory;
import oshi.hardware.platform.linux.LinuxNetworks;
import oshi.hardware.platform.mac.MacGlobalMemory;
import oshi.hardware.platform.mac.MacNetworks;
import oshi.hardware.platform.unix.freebsd.FreeBsdGlobalMemory;
import oshi.hardware.platform.unix.freebsd.FreeBsdNetworks;
import oshi.hardware.platform.windows.WindowsGlobalMemory;
import oshi.hardware.platform.windows.WindowsNetworks;

import java.lang.management.ManagementFactory;
import java.util.*;

public class SystemMetrics {

    private static OperatingSystemMXBean operatingSystemMXBean =
            (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private static AbstractGlobalMemory globalMemory;
    private static AbstractNetworks networks;
    private static long prevBytesReceived = 0;
    private static long prevBytesSent = 0;
    private static long inBoundRate = 0;
    private static long outBoundRate = 0;
    private static final int interval = 10;

    protected static void initSystemMetrics(){
        operatingSystemMXBean.getSystemCpuLoad();
        operatingSystemMXBean.getFreePhysicalMemorySize();
        operatingSystemMXBean.getTotalPhysicalMemorySize();

        if (SystemUtils.IS_OS_UNIX){
            networks = new MacNetworks();
            globalMemory = new MacGlobalMemory();
        }
        else if (SystemUtils.IS_OS_LINUX){
            networks = new LinuxNetworks();
            globalMemory = new LinuxGlobalMemory();
        }
        else if (SystemUtils.IS_OS_FREE_BSD || SystemUtils.IS_OS_OPEN_BSD){
            networks = new FreeBsdNetworks();
            globalMemory = new FreeBsdGlobalMemory();
        }
        else if (SystemUtils.IS_OS_WINDOWS){
            networks = new WindowsNetworks();
            globalMemory = new WindowsGlobalMemory();
        }

        for(NetworkIF network: networks.getNetworks()){
            prevBytesReceived += network.getBytesRecv();
            prevBytesSent += network.getBytesSent();
        }
    }

    // We will run this function every 10 sec to update the system metrics network data
    protected static void updateSystemMetric(Timer timer){

        TimerTask repeatedTask = new TimerTask() {
            long currentBytesReceived;
            long currentBytesSent;
            public void run() {
                currentBytesReceived = 0;
                currentBytesSent = 0;
                for(NetworkIF network: networks.getNetworks()){
                    network.updateNetworkStats();
                    currentBytesReceived += network.getBytesRecv();
                    currentBytesSent += network.getBytesSent();
                }
                inBoundRate = (currentBytesReceived - prevBytesReceived)/interval;
                outBoundRate = (currentBytesSent - prevBytesSent)/interval;
                prevBytesReceived = currentBytesReceived;
                prevBytesSent = currentBytesSent;
            }
        };
        timer.scheduleAtFixedRate(repeatedTask, 1000L, 10000L);
    }

    protected static Map<String, Object> getSystemMetrics(){
        try {
            Map<String, Object> obj = new HashMap<>();
            obj.put("outboundNetwork", outBoundRate);
            obj.put("inboundNetwork", inBoundRate);
            obj.put("memoryUsage",
                    100*(1-((double)globalMemory.getAvailable()/(double)globalMemory.getTotal())));
            obj.put("cpuUsage", operatingSystemMXBean.getSystemCpuLoad()*100);

            return obj;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return null;
    }
}
