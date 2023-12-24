package io.kenji.courier.reflect.cglib;

import io.kenji.courier.reflect.api.ReflectInvoker;
import io.kenji.courier.spi.annotation.SPIClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-23
 **/
@SPIClass
@Slf4j
public class CglibReflectInvoker implements ReflectInvoker {
    @Override
    public Object invokeMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Exception {
        log.info("Invoke method using Cglib reflect type...");
        FastClass fastClass = FastClass.create(serviceClass);
        FastMethod method = fastClass.getMethod(methodName, parameterTypes);
        return method.invoke(serviceBean, parameters);
    }
}
