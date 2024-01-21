package io.kenji.courier.demo.provider;

import io.kenji.courier.annotation.ReflectType;
import io.kenji.courier.annotation.RegisterType;
import io.kenji.courier.provider.RpcSingleServer;

import java.util.concurrent.TimeUnit;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2024-01-05
 **/
public class ProviderNativeDemo {

    public static void main(String[] args) {
        RpcSingleServer rpcSingleServer = RpcSingleServer.builder()
                .serverAddress("127.0.0.1:27880")
                .registryAddress("127.0.0.1:2181")
                .registerType(RegisterType.ZOOKEEPER)
                .scanPackage("io.kenji.courier.demo")
                .reflectType(ReflectType.ASM)
                .heartbeatInterval(10)
                .heartbeatIntervalTimeUnit(TimeUnit.SECONDS)
                .scanNotActiveChannelInterval(10)
                .scanNotActiveChannelIntervalTimeUnit(TimeUnit.SECONDS)
                .enableResultCache(false)
                .resultCacheExpire(3000)
                .build();
        rpcSingleServer.startNettyServer();
    }
}
