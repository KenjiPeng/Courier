package io.kenji.courier.proxy.jdk;

import io.kenji.courier.annotation.SerializationType;
import io.kenji.courier.proxy.api.consumer.Consumer;
import io.kenji.courier.proxy.api.object.ObjectProxy;
import lombok.AllArgsConstructor;

import java.lang.reflect.Proxy;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-17
 **/
@AllArgsConstructor
public class JdkProxyFactory<T> {

    private String serviceVersion;

    private String serviceGroup;
    /**
     * 15s by default
     */
    private long timeout = 15000;

    private Consumer consumer;

    private SerializationType serializationType;

    private boolean async;

    private boolean oneway;


    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),
                new Class[]{clazz},
                new ObjectProxy<>(clazz, serviceVersion, serviceGroup, timeout, consumer, serializationType, async, oneway)
        );
    }
}
