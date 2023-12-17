package io.kenji.courier.proxy.jdk;

import io.kenji.courier.proxy.api.BaseProxyFactory;

import java.lang.reflect.Proxy;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-17
 **/
public class JdkProxyFactory<T> extends BaseProxyFactory<T> {
    @Override
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),
                new Class[]{clazz},
                objectProxy
        );
    }
}
