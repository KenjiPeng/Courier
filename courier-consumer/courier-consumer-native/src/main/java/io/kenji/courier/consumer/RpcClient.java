package io.kenji.courier.consumer;

import io.kenji.courier.annotation.RegisterType;
import io.kenji.courier.annotation.SerializationType;
import io.kenji.courier.consumer.common.RpcConsumer;
import io.kenji.courier.proxy.api.ProxyFactory;
import io.kenji.courier.proxy.api.async.IAsyncObjectProxy;
import io.kenji.courier.proxy.api.config.ProxyConfig;
import io.kenji.courier.proxy.api.object.ObjectProxy;
import io.kenji.courier.proxy.jdk.JdkProxyFactory;
import io.kenji.courier.registry.api.RegistryService;
import io.kenji.courier.registry.api.config.RegistryConfig;
import io.kenji.courier.registry.zookeeper.ZookeeperRegistryService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-17
 **/
@Slf4j
@AllArgsConstructor
public class RpcClient<T> {

    private String registryAddress;

    private RegisterType registerType;

    private String serviceVersion;

    private String serviceGroup;

    private long timeout;

    private SerializationType serializationType;

    private Boolean async;

    private Boolean oneway;


    public <T> T create(Class<T> interfaceClass) {
        ProxyFactory proxyFactory = new JdkProxyFactory<>();
        proxyFactory.init(new ProxyConfig<>(interfaceClass, serviceVersion, serviceGroup, timeout, RpcConsumer.getInstance(), serializationType, async, oneway, getRegistryService(registryAddress, registerType)));
        return proxyFactory.getProxy(interfaceClass);
    }

    public <T> IAsyncObjectProxy createAsync(Class<T> interfaceClass) {
        return new ObjectProxy<T>(interfaceClass, serviceVersion, serviceGroup, timeout, RpcConsumer.getInstance(), serializationType, async, oneway, getRegistryService(registryAddress, registerType));
    }

    private RegistryService getRegistryService(String registryAddress, RegisterType registerType) {
        if (StringUtils.isBlank(registryAddress) || registerType == null) {
            throw new IllegalArgumentException("Registry info is illegal, registryAddress = " + registryAddress + " ,registryService = " + registryAddress);
        }
        ZookeeperRegistryService registryService = new ZookeeperRegistryService();
        try {
            registryService.init(new RegistryConfig(registryAddress, registerType));
        } catch (Exception e) {
            log.error("Hit exception during RpClient init registry service", e);
            throw new RuntimeException(e);
        }
        return registryService;
    }

    public void shutdown() {
        RpcConsumer.getInstance().close();
    }
}
