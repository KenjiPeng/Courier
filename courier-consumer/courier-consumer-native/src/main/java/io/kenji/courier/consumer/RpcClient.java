package io.kenji.courier.consumer;

import io.kenji.courier.annotation.Proxy;
import io.kenji.courier.annotation.RegisterType;
import io.kenji.courier.annotation.SerializationType;
import io.kenji.courier.constants.RegistryLoadBalanceType;
import io.kenji.courier.consumer.common.consumer.RpcConsumer;
import io.kenji.courier.proxy.api.ProxyFactory;
import io.kenji.courier.proxy.api.async.IAsyncObjectProxy;
import io.kenji.courier.proxy.api.config.ProxyConfig;
import io.kenji.courier.proxy.api.object.ObjectProxy;
import io.kenji.courier.registry.api.RegistryService;
import io.kenji.courier.registry.api.config.RegistryConfig;
import io.kenji.courier.spi.loader.ExtensionLoader;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-17
 **/
@Slf4j
@Builder
public class RpcClient<T> {

    private String registryAddress;

    private RegisterType registerType;

    private RegistryLoadBalanceType registryLoadBalanceType;

    private String serviceVersion;

    private String serviceGroup;

    private long requestTimeoutInMilliseconds;

    private SerializationType serializationType;

    private Proxy proxy;

    private Boolean async;

    private Boolean oneway;

    private int heartbeatInterval;

    private TimeUnit heartbeatIntervalTimeUnit;

    private int scanNotActiveChannelInterval;

    private TimeUnit scanNotActiveChannelIntervalTimeUnit;

    private RpcConsumer rpcConsumer;

    private int retryIntervalInMillisecond;

    private int maxRetryTime;
    public <T> T create(Class<T> interfaceClass) {
        ProxyFactory proxyFactory = ExtensionLoader.getExtension(ProxyFactory.class, proxy.name());
        rpcConsumer = RpcConsumer.getInstance(heartbeatInterval, heartbeatIntervalTimeUnit, scanNotActiveChannelInterval, scanNotActiveChannelIntervalTimeUnit, retryIntervalInMillisecond, maxRetryTime);
        proxyFactory.init(new ProxyConfig<>(interfaceClass, serviceVersion, serviceGroup, requestTimeoutInMilliseconds, rpcConsumer, serializationType, async, oneway, getRegistryService(registryAddress, registerType, registryLoadBalanceType)));
        return proxyFactory.getProxy(interfaceClass);
    }

    public <T> IAsyncObjectProxy createAsync(Class<T> interfaceClass) {
        rpcConsumer = RpcConsumer.getInstance(heartbeatInterval, heartbeatIntervalTimeUnit, scanNotActiveChannelInterval, scanNotActiveChannelIntervalTimeUnit, retryIntervalInMillisecond, maxRetryTime);
        return new ObjectProxy<>(interfaceClass, serviceVersion, serviceGroup, requestTimeoutInMilliseconds, rpcConsumer, serializationType, async, oneway, getRegistryService(registryAddress, registerType, registryLoadBalanceType));
    }

    private RegistryService getRegistryService(String registryAddress, RegisterType registerType, RegistryLoadBalanceType registryLoadBalanceType) {
        if (StringUtils.isBlank(registryAddress) || registerType == null) {
            throw new IllegalArgumentException("Registry info is illegal, registryAddress = " + registryAddress + " ,registryService = " + registryAddress);
        }
        RegistryService registryService = ExtensionLoader.getExtension(RegistryService.class, registerType.name());
        try {
            registryService.init(new RegistryConfig(registryAddress, registerType, registryLoadBalanceType));
        } catch (Exception e) {
            log.error("Hit exception during RpClient init registry service", e);
            throw new RuntimeException(e);
        }
        return registryService;
    }

    public void shutdown() {
        rpcConsumer.close();
    }
}
