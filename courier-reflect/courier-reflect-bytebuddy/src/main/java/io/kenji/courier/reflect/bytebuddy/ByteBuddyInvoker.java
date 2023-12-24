package io.kenji.courier.reflect.bytebuddy;

import io.kenji.courier.reflect.api.ReflectInvoker;
import io.kenji.courier.spi.annotation.SPIClass;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;

import java.lang.reflect.Method;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-23
 **/
@SPIClass
@Slf4j
public class ByteBuddyInvoker implements ReflectInvoker {
    @Override
    public Object invokeMethod(Object serviceBean, Class<?> serviceClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Exception {
        log.info("Invoke method using ByteBuddy...");
        try(DynamicType.Unloaded<?> unloaded = new ByteBuddy().subclass(serviceClass)
                    .make()) {
            Class<?> childClass = unloaded.load(this.getClass().getClassLoader())
                    .getLoaded();
            Object instance = childClass.getDeclaredConstructor().newInstance();
            Method method = childClass.getMethod(methodName, parameterTypes);
            method.setAccessible(false);
            return method.invoke(instance,parameters);
        }
    }
}
