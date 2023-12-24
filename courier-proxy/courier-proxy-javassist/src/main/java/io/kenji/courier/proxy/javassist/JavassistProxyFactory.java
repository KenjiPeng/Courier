package io.kenji.courier.proxy.javassist;

import io.kenji.courier.proxy.api.BaseProxyFactory;
import io.kenji.courier.proxy.api.ProxyFactory;
import io.kenji.courier.spi.annotation.SPIClass;
import javassist.util.proxy.Proxy;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-23
 **/
@SPIClass
@Slf4j
public class JavassistProxyFactory<T> extends BaseProxyFactory<T> implements ProxyFactory {

    private final javassist.util.proxy.ProxyFactory proxyFactory = new javassist.util.proxy.ProxyFactory();

    @Override
    public <T> T getProxy(Class<T> clazz) {
        log.info("Get proxy using Javassist...");
        proxyFactory.setInterfaces(new Class[]{clazz});
        Proxy proxy = null;
        try {
            proxy = (Proxy) proxyFactory.createClass().getDeclaredConstructor().newInstance();
            proxy.setHandler((self, thisMethod, proceed, args) -> objectProxy.invoke(self, thisMethod, args));
        } catch (Exception e) {
            log.error("Failed to create Javassist proxy", e);
        }
        return (T) proxy;
    }
}
