package io.kenji.courier.consumer;

import io.kenji.courier.annotation.SerializationType;
import io.kenji.courier.consumer.common.RpcConsumer;
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

    private boolean async;

    private boolean oneway;

    public <T> T create(Class<T> interfaceClass){
        JdkProxyFactory<T> jdkProxyFactory = new JdkProxyFactory<>(serviceVersion, serviceGroup, timeout, RpcConsumer.getInstance(), serializationType, async, oneway);
        return jdkProxyFactory.getProxy(interfaceClass);
    }

    public void shutdown(){
        RpcConsumer.getInstance().close();
    }
}
