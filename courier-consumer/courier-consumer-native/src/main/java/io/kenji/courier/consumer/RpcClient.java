package io.kenji.courier.consumer;

import io.kenji.courier.annotation.ProxyType;
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
public class RpcClient {

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

    private int retryIntervalInMillisecond;

    private int maxRetryTime;

    private boolean enableResultCache;

    private int resultCacheExpire;

    private boolean enableDirectServer;

    private String directServerUrl;

    public RpcClient(String registryAddress, RegisterType registerType, RegistryLoadBalanceType registryLoadBalanceType, String serviceVersion, String serviceGroup,
                     long requestTimeoutInMilliseconds, SerializationType serializationType, ProxyType proxyType, Boolean async, Boolean oneway, int heartbeatInterval,
                     TimeUnit heartbeatIntervalTimeUnit, int scanNotActiveChannelInterval, TimeUnit scanNotActiveChannelIntervalTimeUnit,
                     int retryIntervalInMillisecond, int maxRetryTime, boolean enableResultCache, int resultCacheExpire, boolean enableDirectServer, String directServerUrl) {
        this.registryAddress = registryAddress;
        this.registerType = registerType;
        this.registryLoadBalanceType = registryLoadBalanceType;
        this.serviceVersion = serviceVersion;
        this.serviceGroup = serviceGroup;
        this.requestTimeoutInMilliseconds = requestTimeoutInMilliseconds;
        this.serializationType = serializationType;
        this.proxyType = proxyType;
        this.async = async;
        this.oneway = oneway;
        this.heartbeatInterval = heartbeatInterval;
        this.heartbeatIntervalTimeUnit = heartbeatIntervalTimeUnit;
        this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
        this.scanNotActiveChannelIntervalTimeUnit = scanNotActiveChannelIntervalTimeUnit;
        this.retryIntervalInMillisecond = retryIntervalInMillisecond;
        this.maxRetryTime = maxRetryTime;
        this.enableResultCache = enableResultCache;
        this.resultCacheExpire = resultCacheExpire;
        this.enableDirectServer = enableDirectServer;
        this.directServerUrl = directServerUrl;
    }

    public <T> T create(Class<T> interfaceClass) {
        ProxyFactory proxyFactory = ExtensionLoader.getExtension(ProxyFactory.class, proxyType.name());
        proxyFactory.init(new ProxyConfig<>(interfaceClass, serviceVersion, serviceGroup, requestTimeoutInMilliseconds,
                RpcConsumer.getInstance(heartbeatInterval, heartbeatIntervalTimeUnit, scanNotActiveChannelInterval,
                        scanNotActiveChannelIntervalTimeUnit, retryIntervalInMillisecond, maxRetryTime, enableDirectServer, directServerUrl),
                serializationType, async, oneway, getRegistryService(registryAddress, registerType, registryLoadBalanceType), enableResultCache, resultCacheExpire));
        return proxyFactory.getProxy(interfaceClass);
    }

    public <T> IAsyncObjectProxy createAsync(Class<T> interfaceClass) {
        return new ObjectProxy<>(interfaceClass, serviceVersion, serviceGroup, requestTimeoutInMilliseconds,
                RpcConsumer.getInstance(heartbeatInterval, heartbeatIntervalTimeUnit, scanNotActiveChannelInterval,
                        scanNotActiveChannelIntervalTimeUnit, retryIntervalInMillisecond, maxRetryTime, enableDirectServer, directServerUrl),
                serializationType, async, oneway, getRegistryService(registryAddress, registerType, registryLoadBalanceType), enableResultCache, resultCacheExpire);
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
        RpcConsumer.getInstance(heartbeatInterval, heartbeatIntervalTimeUnit, scanNotActiveChannelInterval,
                scanNotActiveChannelIntervalTimeUnit, retryIntervalInMillisecond, maxRetryTime, enableDirectServer, directServerUrl).close();
    }
}
