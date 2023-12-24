package io.kenji.courier.proxy.jdk;

import io.kenji.courier.proxy.api.BaseProxyFactory;
import io.kenji.courier.proxy.api.ProxyFactory;
import io.kenji.courier.spi.annotation.SPIClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-17
 **/
@SPIClass
@Slf4j
public class JdkProxyFactory<T> extends BaseProxyFactory<T>  implements ProxyFactory {
    @Override
    public <T> T getProxy(Class<T> clazz) {
        log.info("Get proxy using JDK proxy...");
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),
                new Class[]{clazz},
                objectProxy
        );
    }
}
