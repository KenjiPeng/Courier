package io.kenji.courier.reflect.jdk;

import io.kenji.courier.reflect.api.ReflectInvoker;
import io.kenji.courier.spi.annotation.SPIClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-23
 **/
@SPIClass
@Slf4j
public class JdkReflectInvoker implements ReflectInvoker {

    @Override
    public Object invokeMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Exception {
        log.info("Invoke method using JDK reflect type...");
        Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(serviceBean,parameters);
    }
}
