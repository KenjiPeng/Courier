package io.kenji.courier.proxy.api;

import io.kenji.courier.proxy.api.config.ProxyConfig;
import io.kenji.courier.proxy.api.object.ObjectProxy;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-17
 **/
public abstract class BaseProxyFactory<T> implements ProxyFactory {
    protected ObjectProxy<T> objectProxy;

    @Override
    public <T> void init(ProxyConfig<T> proxyConfig) {
        this.objectProxy = new ObjectProxy(proxyConfig.clazz(),
                proxyConfig.serviceVersion(),
                proxyConfig.serviceGroup(),
                proxyConfig.timeout(),
                proxyConfig.consumer(),
                proxyConfig.serializationType(),
                proxyConfig.async(),
                proxyConfig.oneway(),
                proxyConfig.registryService());
    }
}
