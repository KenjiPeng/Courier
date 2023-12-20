package io.kenji.courier.serialization.api;

import io.kenji.courier.constants.RpcConstants;
import io.kenji.courier.spi.annotation.SPI;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/25
 **/
@SPI(RpcConstants.SERIALIZATION_TYPE_JDK)
public interface Serialization {
    /**
     * Serialize
     * @param obj
     * @return
     * @param <T>
     */
    <T> byte[] serialize(T obj);

    /**
     * Deserialize
     * @param data
     * @param cls
     * @return
     * @param <T>
     */
    <T> T deserialize(byte[] data,Class<T> cls);
}
