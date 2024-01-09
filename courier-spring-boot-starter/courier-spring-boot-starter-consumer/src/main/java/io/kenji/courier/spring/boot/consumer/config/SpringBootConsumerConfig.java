package io.kenji.courier.spring.boot.consumer.config;

import io.kenji.courier.annotation.ProxyType;
import io.kenji.courier.annotation.RegisterType;
import io.kenji.courier.annotation.SerializationType;
import io.kenji.courier.constants.RegistryLoadBalanceType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.concurrent.TimeUnit;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2024-01-09
 **/
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SpringBootConsumerConfig {

    private String registryAddress;

    private RegisterType registerType;

    private RegistryLoadBalanceType registryLoadBalanceType;

    private String serviceVersion;

    private String serviceGroup;

    private long requestTimeoutInMilliseconds;

    private SerializationType serializationType;

    private ProxyType proxyType;

    private Boolean async;

    private Boolean oneway;

    private int heartbeatInterval;

    private TimeUnit heartbeatIntervalTimeUnit;

    private int scanNotActiveChannelInterval;

    private TimeUnit scanNotActiveChannelIntervalTimeUnit;

    private int retryIntervalInMillisecond = 1000;

    private int maxRetryTime = 3;

}
