package io.kenji.courier.common.utils;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.net.InetAddress;
import java.net.InetSocketAddress;
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
    public static Pair<String,Integer> getRemoteAddressAndPort(Channel channel){
        InetSocketAddress remoteAddress = (InetSocketAddress) channel.remoteAddress();
        String address = remoteAddress.getAddress().getHostAddress();
        int port = remoteAddress.getPort();
        return Pair.of(address,port);
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
