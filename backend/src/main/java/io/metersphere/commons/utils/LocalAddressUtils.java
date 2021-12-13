package io.metersphere.commons.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class LocalAddressUtils {

    /**
     * 这个过滤了 是回路的地址
     */
    public static Map<String, String> getIpAddresses() {
        Enumeration<NetworkInterface> netInterfaces;
        Map<String, String> result = new HashMap<>();
        try {
            // 拿到所有网卡
            netInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip;
            // 遍历每个网卡，拿到ip
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    ip = addresses.nextElement();
                    if (!ip.isLoopbackAddress() && ip.getHostAddress().indexOf(':') == -1) {
                        result.put(ni.getName(), ip.getHostAddress());
                    }
                }
            }
        } catch (Exception e) {
        }
        return result;
    }

    public static String getIpAddress(String name) {
        return getIpAddresses().get(name);
    }

}
