package io.kenji.courier.proxy.cglib;

import io.kenji.courier.proxy.api.BaseProxyFactory;
import io.kenji.courier.proxy.api.ProxyFactory;
import io.kenji.courier.spi.annotation.SPIClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InvocationHandler;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-23
 **/
@SPIClass
@Slf4j
public class CglibProxyFactory<T> extends BaseProxyFactory<T> implements ProxyFactory {
    private final Enhancer enhancer = new Enhancer();

    @Override
    public <T> T getProxy(Class<T> clazz) {
        log.info("Get proxy using cglib...");
        enhancer.setInterfaces(new Class[]{clazz});
        enhancer.setCallback((InvocationHandler) (proxy, method, args) -> objectProxy.invoke(proxy,method,args));
        return (T) enhancer.create();
    }
}
