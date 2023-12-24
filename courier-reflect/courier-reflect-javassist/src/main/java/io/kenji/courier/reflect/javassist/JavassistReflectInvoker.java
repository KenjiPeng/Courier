package io.kenji.courier.reflect.javassist;

import io.kenji.courier.reflect.api.ReflectInvoker;
import io.kenji.courier.spi.annotation.SPIClass;
import javassist.util.proxy.ProxyFactory;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-23
 **/
@SPIClass
@Slf4j
public class JavassistReflectInvoker implements ReflectInvoker {
    @Override
    public Object invokeMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Exception {
        log.info("Invoke method using Javassist reflect type...");
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setSuperclass(serviceClass);
        Class<?> childClass = proxyFactory.createClass();
        Method method = childClass.getMethod(methodName, parameterTypes);
        method.setAccessible(false);
        return method.invoke(childClass.getDeclaredConstructor().newInstance(), parameters);
    }
}
