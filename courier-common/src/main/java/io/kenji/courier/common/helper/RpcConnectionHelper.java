package io.kenji.courier.common.helper;

import io.kenji.courier.common.utils.IpUtils;
import io.netty.channel.Channel;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2024-01-01
 **/
public class RpcConnectionHelper {
    public static String buildConnectionKey(String serviceAddr, int servicePort) {
        return serviceAddr.concat("_").concat(String.valueOf(servicePort));
    }

    public static String buildConnectionKey(Channel channel) {
        Pair<String, Integer> remoteAddressAndPort = IpUtils.getRemoteAddressAndPort(channel);
        return buildConnectionKey(remoteAddressAndPort.getKey(),remoteAddressAndPort.getValue());
    }

}
