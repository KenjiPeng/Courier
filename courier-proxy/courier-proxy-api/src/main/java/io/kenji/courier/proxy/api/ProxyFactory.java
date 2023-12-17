package io.kenji.courier.proxy.api;

import io.kenji.courier.proxy.api.config.ProxyConfig;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-17
 **/
public interface ProxyFactory {

    <T> T getProxy(Class<T> clazz);

    default <T> void init(ProxyConfig<T> proxyConfig){}
}
