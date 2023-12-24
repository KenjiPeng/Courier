package io.kenji.courier.proxy.asm.factory;

import io.kenji.courier.proxy.api.BaseProxyFactory;
import io.kenji.courier.proxy.api.ProxyFactory;
import io.kenji.courier.proxy.asm.proxy.ASMProxy;
import io.kenji.courier.spi.annotation.SPIClass;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-23
 **/
@SPIClass
@Slf4j
public class ASMProxyFactory<T> extends BaseProxyFactory<T> implements ProxyFactory {
    @Override
    public <T> T getProxy(Class<T> clazz) {
        log.info("Get proxy using ASM...");
        try {
            return (T) ASMProxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{clazz}, objectProxy);
        } catch (Exception e) {
            log.error("Failed to get proxy", e);
        }
        return null;
    }
}
