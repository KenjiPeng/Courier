package io.kenji.courier.demo.consumer;

import io.kenji.courier.annotation.ProxyType;
import io.kenji.courier.annotation.RegisterType;
import io.kenji.courier.annotation.SerializationType;
import io.kenji.courier.constants.RegistryLoadBalanceType;
import io.kenji.courier.consumer.RpcClient;
import io.kenji.courier.demo.api.DemoService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2024-01-05
 **/
@Slf4j
public class ConsumerNativeDemo {

    public static void main(String[] args) {
        RpcClient rpcClient = RpcClient.builder()
                .registryAddress("127.0.0.1:2181")
                .registerType(RegisterType.ZOOKEEPER)
                .registryLoadBalanceType(RegistryLoadBalanceType.LEAST_CONNECTION_ENHANCE)
                .serviceVersion("1.0.0")
                .serviceGroup("Kenji")
                .requestTimeoutInMilliseconds(10000000)
                .serializationType(SerializationType.PROTOSTUFF)
                .proxyType(ProxyType.ASM)
                .async(false)
                .oneway(false)
                .heartbeatInterval(10)
                .heartbeatIntervalTimeUnit(TimeUnit.SECONDS)
                .scanNotActiveChannelInterval(10)
                .scanNotActiveChannelIntervalTimeUnit(TimeUnit.SECONDS).build();
        DemoService demoService = rpcClient.create(DemoService.class);
        String result = demoService.hello("Kenji");
        log.info("Got result: {}", result);
        rpcClient.shutdown();
    }
}
