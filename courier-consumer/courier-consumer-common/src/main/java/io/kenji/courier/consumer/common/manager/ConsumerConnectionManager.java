package io.kenji.courier.consumer.common.manager;

import io.kenji.courier.annotation.SerializationType;
import io.kenji.courier.common.helper.RpcConnectionHelper;
import io.kenji.courier.common.utils.IpUtils;
import io.kenji.courier.constants.RpcConstants;
import io.kenji.courier.consumer.common.RpcConsumer;
import io.kenji.courier.consumer.common.cache.ConsumerChannelCache;
import io.kenji.courier.consumer.common.helper.RpcConsumerHandlerHelper;
import io.kenji.courier.protocol.RpcProtocol;
import io.kenji.courier.protocol.enumeration.RpcType;
import io.kenji.courier.protocol.header.RpcHeaderFactory;
import io.kenji.courier.protocol.request.RpcRequest;
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
 * @Date 2023-12-27
 **/
@Slf4j
public class ConsumerConnectionManager {

    private static volatile Map<String, AtomicInteger> heartbeatRecordMap = new ConcurrentHashMap<>();

    private static final int HEARTBEAT_TIME_THRESHOLD_PRE_CHANNEL = 3;
    private static final int HEARTBEAT_RECONNECT_TIME_THRESHOLD_PRE_CHANNEL = 2;

    public static void scanNotActiveChannel() {
        Set<Channel> channelCache = ConsumerChannelCache.getChannelCache();
        if (channelCache == null || channelCache.size() == 0) return;
        for (Channel channel : channelCache) {
            if (!channel.isOpen() || !channel.isActive()) {
                channel.close();
                ConsumerChannelCache.removeChannelFromCache(channel);
            }
        }
        Set<String> remoteServiceAddress = ConsumerChannelCache.getRemoteServiceAddress();
        for (String key : heartbeatRecordMap.keySet()) {
            if (!remoteServiceAddress.contains(key)){
                heartbeatRecordMap.remove(key);
            }
        }
    }

    public static void broadcastPingMessageFromConsumer(RpcConsumer rpcConsumer) {
        Set<Channel> channelCache = ConsumerChannelCache.getChannelCache();
        if (channelCache == null || channelCache.size() == 0) return;
        RpcRequest request = new RpcRequest();
        request.setParameters(new Object[]{RpcConstants.HEART_BEAT_PING});
        RpcProtocol<RpcRequest> rpcProtocol = RpcProtocol.<RpcRequest>builder()
                .header(RpcHeaderFactory.getRpcProtocolHeader(SerializationType.PROTOSTUFF, RpcType.HEARTBEAT_REQUEST_FROM_CONSUMER.getType()))
                .body(request)
                .build();
        for (Channel channel : channelCache) {
            Pair<String, Integer> remoteAddressAndPort = IpUtils.getRemoteAddressAndPort(channel);
            String address = remoteAddressAndPort.getKey();
            int port = remoteAddressAndPort.getValue();
            if (isOverHeartbeatMaxTimes(RpcConnectionHelper.buildConnectionKey(address, port))) {
                log.warn("Heart beat retry time arrive threshold, will close connection");
                heartbeatRecordMap.remove(RpcConnectionHelper.buildConnectionKey(address, port));
                clearFailConnection(address, port, channel);
                continue;
            }
            // Reconnect
            if (arriveHeartbeatThreshold(RpcConnectionHelper.buildConnectionKey(address, port))) {
                log.warn("Heart beat arrive threshold, will reconnect");
                clearFailConnection(address, port, channel);
                reconnectToProvider(rpcConsumer, address, port);
                addHeartbeatRecord(address, port);

            }
            if (channel.isActive() && channel.isOpen()) {
                log.info("Send heartbeat message to service provider, the provider is {}, the heartbeat message is {}", channel.remoteAddress(), RpcConstants.HEART_BEAT_PING);
                channel.writeAndFlush(rpcProtocol);
                addHeartbeatRecord(address, port);
            }
        }
    }

    private static boolean isOverHeartbeatMaxTimes(String key) {
        return heartbeatRecordMap.get(key) != null && heartbeatRecordMap.get(key).get() > HEARTBEAT_TIME_THRESHOLD_PRE_CHANNEL *HEARTBEAT_RECONNECT_TIME_THRESHOLD_PRE_CHANNEL- 1;
    }

    private static void addHeartbeatRecord(String address, int port) {
        heartbeatRecordMap.computeIfAbsent(RpcConnectionHelper.buildConnectionKey(address, port), k -> new AtomicInteger(0)).incrementAndGet();
    }

    private static void reconnectToProvider(RpcConsumer rpcConsumer, String address, int port) {
        try {
            rpcConsumer.getRpcConsumerHandler(address, port);
        } catch (InterruptedException e) {
            log.error("Failed to reconnect to {}:{}", address, port, e);
        }
    }

    public static void clearFailConnection(String address, int port, Channel channel) {
        ConsumerChannelCache.removeChannelFromCache(channel);
        RpcConsumerHandlerHelper.remove(address, port);
        channel.close();
    }

    private static boolean arriveHeartbeatThreshold(String key) {
        return heartbeatRecordMap.get(key) != null && heartbeatRecordMap.get(key).get() == HEARTBEAT_TIME_THRESHOLD_PRE_CHANNEL - 1;
    }

    public static void removeHeartBeatRecord(Channel channel) {
        Pair<String, Integer> remoteAddressAndPort = IpUtils.getRemoteAddressAndPort(channel);
        heartbeatRecordMap.remove(RpcConnectionHelper.buildConnectionKey(remoteAddressAndPort.getKey(), remoteAddressAndPort.getValue()));
    }
}
