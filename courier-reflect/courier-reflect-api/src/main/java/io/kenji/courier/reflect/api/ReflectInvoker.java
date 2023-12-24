package io.kenji.courier.reflect.api;

import io.kenji.courier.spi.annotation.SPI;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-23
 **/
@SPI
public interface ReflectInvoker {

    /**
     * Invoke real method
     * @param serviceBean
     * @param serviceClass
     * @param methodName
     * @param parameterTypes
     * @param parameters
     * @return
     * @throws Exception
     */
    Object invokeMethod(Object serviceBean, Class<?> serviceClass,String methodName,Class<?>[] parameterTypes,Object[] parameters) throws Exception;
}
