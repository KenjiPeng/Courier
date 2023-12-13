package io.kenji.courier.serialization.api;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/25
 **/
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
