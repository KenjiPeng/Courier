package io.kenji.courier.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-25
 **/
@Slf4j
public class IpUtils {


    public static InetAddress getLocalInetAddress() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            log.error("Hit exception during getting local ip address", e);
        }
        return null;
    }

    public static String getLocalHostIp() {
        return getLocalInetAddress().getHostAddress();
    }

    public static String getLocalHostName() {
        return getLocalInetAddress().getHostName();
    }

    public static String getLocalAddress() {
        return getLocalInetAddress().toString();
    }
}
