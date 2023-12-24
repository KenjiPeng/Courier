package io.kenji.courier.proxy.bytebuddy;

import io.kenji.courier.proxy.api.BaseProxyFactory;
import io.kenji.courier.proxy.api.ProxyFactory;
import io.kenji.courier.spi.annotation.SPIClass;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.InvocationHandlerAdapter;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-23
 **/
@Slf4j
@SPIClass
public class ByteBuddyProxyFactory<T> extends BaseProxyFactory<T> implements ProxyFactory {
    @Override
    public <T> T getProxy(Class<T> clazz) {
        log.info("Get proxy using byte buddy...");
        try(DynamicType.Unloaded<Object> unloaded = new ByteBuddy().subclass(Object.class)
                .implement(clazz)
                .intercept(InvocationHandlerAdapter.of(objectProxy))
                .make()) {
            return (T) unloaded.load(ByteBuddyProxyFactory.class.getClassLoader())
                    .getLoaded()
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (Exception e) {
            log.error("Failed to get Byte buddy proxy",e);
        }
        return null;
    }
}
