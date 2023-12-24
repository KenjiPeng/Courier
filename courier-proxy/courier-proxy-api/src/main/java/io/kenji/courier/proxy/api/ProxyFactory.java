package io.kenji.courier.proxy.api;

import io.kenji.courier.proxy.api.config.ProxyConfig;
import io.kenji.courier.spi.annotation.SPI;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-17
 **/
@SPI
public interface ProxyFactory {

    <T> T getProxy(Class<T> clazz);

    default <T> void init(ProxyConfig<T> proxyConfig){}
}
