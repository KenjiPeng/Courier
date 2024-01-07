package io.kenji.courier.demo.spring.annotation.consumer.service.impl;

import io.kenji.courier.annotation.ProxyType;
import io.kenji.courier.annotation.RegisterType;
import io.kenji.courier.annotation.RpcConsumer;
import io.kenji.courier.annotation.SerializationType;
import io.kenji.courier.constants.RegistryLoadBalanceType;
import io.kenji.courier.demo.api.DemoService;
import io.kenji.courier.demo.spring.annotation.consumer.service.ConsumerDemoService;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2024-01-07
 **/
@Service
public class ConsumerDemoServiceImpl implements ConsumerDemoService {

    @RpcConsumer(registerType = RegisterType.ZOOKEEPER, registerAddress = "127.0.0.1:2181", loadBalanceType = RegistryLoadBalanceType.RANDOM, version = "1.0.0",
            serializationType = SerializationType.JDK, requestTimeoutInMilliseconds = 7000, async = false, oneway = false, proxyType = ProxyType.BYTE_BUDDY, group = "Kenji",
            heartbeatInterval = 6, heartbeatIntervalTimeUnit = TimeUnit.SECONDS, scanNotActiveChannelInterval = 1, scanNotActiveChannelIntervalTimeUnit = TimeUnit.MINUTES,
            maxRetryTime = 5, retryIntervalInMillisecond = 2000)
    private DemoService demoService;

    @Override
    public String hello(String name) {
        return demoService.hello(name);
    }
}
