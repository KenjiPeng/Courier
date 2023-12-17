package io.kenji.courier.consumer;

import io.kenji.courier.annotation.SerializationType;
import io.kenji.courier.consumer.common.RpcConsumer;
import io.kenji.courier.proxy.api.ProxyFactory;
import io.kenji.courier.proxy.api.async.IAsyncObjectProxy;
import io.kenji.courier.proxy.api.config.ProxyConfig;
import io.kenji.courier.proxy.api.object.ObjectProxy;
import io.kenji.courier.proxy.jdk.JdkProxyFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-17
 **/
@Slf4j
@AllArgsConstructor
public class RpcClient<T> {

    private String serviceVersion;

    private String serviceGroup;

    private long timeout;

    private SerializationType serializationType;

    private Boolean async;

    private Boolean oneway;

    public <T> T create(Class<T> interfaceClass) {
        ProxyFactory proxyFactory = new JdkProxyFactory<>();
        proxyFactory.init(new ProxyConfig<>(interfaceClass,serviceVersion, serviceGroup, timeout, RpcConsumer.getInstance(), serializationType, async, oneway));
        return proxyFactory.getProxy(interfaceClass);
    }

    public <T> IAsyncObjectProxy createAsync(Class<T> interfaceClass) {
        return new ObjectProxy<T>(interfaceClass, serviceVersion, serviceGroup, timeout, RpcConsumer.getInstance(), serializationType, async, oneway);
    }

    public void shutdown() {
        RpcConsumer.getInstance().close();
    }
}
