package io.kenji.courier.consumer.spring;

import io.kenji.courier.annotation.ProxyType;
import io.kenji.courier.annotation.RegisterType;
import io.kenji.courier.annotation.SerializationType;
import io.kenji.courier.constants.RegistryLoadBalanceType;
import io.kenji.courier.consumer.RpcClient;
import lombok.Setter;
import org.springframework.beans.factory.FactoryBean;

import java.util.concurrent.TimeUnit;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2024-01-06
 **/
@Setter
public class RpcConsumerBean implements FactoryBean<Object> {

    private Class<?> interfaceClass;

    private String registryAddress;

    private RegisterType registerType;

    private RegistryLoadBalanceType registryLoadBalanceType;

    private String serviceVersion;

    private String serviceGroup;

    private long requestTimeoutInMilliseconds;

    private SerializationType serializationType;

    private ProxyType proxyType;

    private Object proxyObject;

    private Boolean async;

    private Boolean oneway;

    private int heartbeatInterval;

    private TimeUnit heartbeatIntervalTimeUnit;

    private int scanNotActiveChannelInterval;

    private TimeUnit scanNotActiveChannelIntervalTimeUnit;

    private int retryIntervalInMillisecond = 1000;

    private int maxRetryTime = 3;

    private boolean enableResultCache;

    private int resultCacheExpire;

    private boolean enableDirectServer;

    private String directServerUrl;

    private boolean enableDelayConnection = false;

    @Override
    public Object getObject() throws Exception {
        return proxyObject;
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceClass;
    }

    public void init() {
        RpcClient rpcClient = RpcClient.builder()
                .registryAddress(registryAddress)
                .registerType(registerType)
                .registryLoadBalanceType(registryLoadBalanceType)
                .proxyType(proxyType)
                .serviceVersion(serviceVersion)
                .serviceGroup(serviceGroup)
                .serializationType(serializationType)
                .requestTimeoutInMilliseconds(requestTimeoutInMilliseconds)
                .async(async)
                .oneway(oneway)
                .heartbeatInterval(heartbeatInterval)
                .heartbeatIntervalTimeUnit(heartbeatIntervalTimeUnit)
                .scanNotActiveChannelInterval(scanNotActiveChannelInterval)
                .scanNotActiveChannelIntervalTimeUnit(scanNotActiveChannelIntervalTimeUnit)
                .retryIntervalInMillisecond(retryIntervalInMillisecond)
                .maxRetryTime(maxRetryTime)
                .enableResultCache(enableResultCache)
                .resultCacheExpire(resultCacheExpire)
                .enableDirectServer(enableDirectServer)
                .directServerUrl(directServerUrl)
                .enableDelayConnection(enableDelayConnection)
                .build();
        this.proxyObject = rpcClient.create(interfaceClass);
    }
}
