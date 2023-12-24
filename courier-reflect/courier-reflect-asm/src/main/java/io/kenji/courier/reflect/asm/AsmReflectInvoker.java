package io.kenji.courier.reflect.asm;

import io.kenji.courier.reflect.api.ReflectInvoker;
import io.kenji.courier.reflect.asm.proxy.ReflectProxy;
import io.kenji.courier.spi.annotation.SPIClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;


/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-24
 **/
@SPIClass
@Slf4j
public class AsmReflectInvoker implements ReflectInvoker {
    @Override
    public Object invokeMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Exception {
        log.info("Invoke method using ASM...");
        Constructor<?> constructor = serviceClass.getConstructor(new Class[]{});
        Object[] constructorParam = new Object[]{};
        Object instance = ReflectProxy.newProxyInstance(AsmReflectInvoker.class.getClassLoader(), getInvocationHandler(serviceBean), serviceClass, constructor, constructorParam);
        Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(instance, parameters);
    }

    private InvocationHandler getInvocationHandler(Object serviceBean) {
        return (proxy, method, args) -> {
            log.info("use proxy invoke method...");
            method.setAccessible(true);
            return method.invoke(serviceBean, args);
        };
    }
}
