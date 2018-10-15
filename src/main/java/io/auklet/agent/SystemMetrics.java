package io.auklet.agent;

import com.sun.management.OperatingSystemMXBean;
import org.apache.commons.lang3.SystemUtils;
import oshi.hardware.common.AbstractGlobalMemory;
import oshi.hardware.platform.linux.LinuxGlobalMemory;
import oshi.hardware.platform.mac.MacGlobalMemory;
import oshi.hardware.platform.unix.freebsd.FreeBsdGlobalMemory;
import oshi.hardware.platform.windows.WindowsGlobalMemory;

import java.lang.management.ManagementFactory;
import java.util.*;

public class SystemMetrics {

    private static OperatingSystemMXBean operatingSystemMXBean =
            (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private static AbstractGlobalMemory globalMemory;

    protected static void initSystemMetrics(){
        operatingSystemMXBean.getSystemCpuLoad();

        if (SystemUtils.IS_OS_MAC){
            globalMemory = new MacGlobalMemory();
        }
        else if (SystemUtils.IS_OS_LINUX){
            globalMemory = new LinuxGlobalMemory();
        }
        else if (SystemUtils.IS_OS_FREE_BSD || SystemUtils.IS_OS_OPEN_BSD){
            globalMemory = new FreeBsdGlobalMemory();
        }
        else if (SystemUtils.IS_OS_WINDOWS){
            globalMemory = new WindowsGlobalMemory();
        }
    }

    protected static Map<String, Object> getSystemMetrics(){
        try {
            Map<String, Object> obj = new HashMap<>();
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
