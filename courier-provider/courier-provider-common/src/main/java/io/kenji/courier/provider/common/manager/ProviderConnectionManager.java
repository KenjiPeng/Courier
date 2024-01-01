package io.kenji.courier.provider.common.manager;

import io.kenji.courier.annotation.SerializationType;
import io.kenji.courier.common.helper.RpcConnectionHelper;
import io.kenji.courier.common.utils.IpUtils;
import io.kenji.courier.constants.RpcConstants;
import io.kenji.courier.protocol.RpcProtocol;
import io.kenji.courier.protocol.enumeration.RpcType;
import io.kenji.courier.protocol.header.RpcHeaderFactory;
import io.kenji.courier.protocol.response.RpcResponse;
import io.kenji.courier.provider.common.cache.ProviderChannelCache;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-31
 **/
@Slf4j
public class ProviderConnectionManager {

    private static volatile Map<Channel, AtomicInteger> heartbeatRecordMap = new ConcurrentHashMap<>();

    private static final int HEARTBEAT_TIME_THRESHOLD_PRE_CHANNEL = 3;

    public static void scanNotActiveChannel() {
        Set<Channel> channelCache = ProviderChannelCache.getChannelCache();
        if (channelCache == null || channelCache.size() == 0) return;
        for (Channel channel : channelCache) {
            if (!channel.isOpen() || !channel.isActive()) {
                channel.close();
                ProviderChannelCache.remove(channel);
                log.info("Remove channel successfully, remote address: {}", channel.remoteAddress());
            }
        }
    }

    public static void broadcastPingMessageFromProvider() {
        Set<Channel> channelCache = ProviderChannelCache.getChannelCache();
        if (channelCache == null || channelCache.size() == 0) return;
        RpcResponse response = new RpcResponse();
        response.setResult(RpcConstants.HEART_BEAT_PING);
        RpcProtocol<RpcResponse> rpcProtocol = RpcProtocol.<RpcResponse>builder()
                .header(RpcHeaderFactory.getRpcProtocolHeader(SerializationType.PROTOSTUFF, RpcType.HEARTBEAT_REQUEST_FROM_PROVIDER.getType()))
                .body(response)
                .build();
        for (Channel channel : channelCache) {
            Pair<String, Integer> remoteAddressAndPort = IpUtils.getRemoteAddressAndPort(channel);
            String key = RpcConnectionHelper.buildConnectionKey(remoteAddressAndPort.getKey(), remoteAddressAndPort.getValue());
            if (arriveHeartbeatThreshold(channel)) {
                log.warn("Heart beat arrive threshold, will close the connection");
                closeChannel(key, channel);
                continue;
            }
            if (channel.isActive() && channel.isOpen()) {
                log.info("Send heartbeat message to service consumer, the consumer is {}, the heartbeat message is {}", channel.remoteAddress(), RpcConstants.HEART_BEAT_PING);
                channel.writeAndFlush(rpcProtocol);
                addHeartbeatRecord(channel);
            }
        }
    }

    private static void closeChannel(String key, Channel channel) {
        ProviderChannelCache.remove(channel);
        heartbeatRecordMap.remove(channel);
        channel.close();
    }


    private static boolean arriveHeartbeatThreshold(Channel channel) {
        return heartbeatRecordMap.get(channel) != null && heartbeatRecordMap.get(channel).get() == HEARTBEAT_TIME_THRESHOLD_PRE_CHANNEL - 1;
    }

    private static void addHeartbeatRecord(Channel channel) {
        heartbeatRecordMap.computeIfAbsent(channel, k -> new AtomicInteger(0)).incrementAndGet();
    }

    public static void removeHeartBeatRecord(Channel channel) {
        heartbeatRecordMap.remove(channel);
    }
}
