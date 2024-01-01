package io.kenji.courier.consumer.common.cache;

import io.kenji.courier.common.helper.RpcConnectionHelper;
import io.netty.channel.Channel;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-27
 **/
public class ConsumerChannelCache {
    private static volatile Set<Channel> channelCache = new CopyOnWriteArraySet<>();

    public static void addChannelIntoCache(Channel channel) {
        channelCache.add(channel);
    }

    public static void removeChannelFromCache(Channel channel) {
        channelCache.remove(channel);
    }

    public static Set<Channel> getChannelCache() {
        return channelCache;
    }

    public static Set<String> getRemoteServiceAddress() {
       return channelCache.stream().map(RpcConnectionHelper::buildConnectionKey).collect(Collectors.toSet());
    }

}
