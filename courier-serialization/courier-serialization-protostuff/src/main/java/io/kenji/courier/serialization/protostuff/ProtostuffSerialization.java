package io.kenji.courier.serialization.protostuff;

import io.kenji.courier.common.exception.SerializerException;
import io.kenji.courier.serialization.api.Serialization;
import io.kenji.courier.spi.annotation.SPIClass;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import lombok.extern.slf4j.Slf4j;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-22
 **/
@SPIClass
@Slf4j
public class ProtostuffSerialization implements Serialization {

    private final Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();

    private final Objenesis objenesis = new ObjenesisStd();

    @SuppressWarnings("unchecked")
    private <T> Schema<T> getSchema(Class<T> cls) {
        Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
        if (schema == null) {
            schema = RuntimeSchema.createFrom(cls);
            if (schema != null) {
                cachedSchema.put(cls, schema);
            }
        }
        return schema;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> byte[] serialize(T obj) {
        log.info("Execute protostuff serialize...");
        if (obj == null) {
            log.info("serialize object is null");
            throw new SerializerException("serialize object is null");
        }
        Class<T> cls = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(cls);
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new SerializerException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> cls) {
        log.info("Execute protostuff deserialize...");
        if (data == null) {
            log.info("deserialize data is null");
            throw new SerializerException("deserialize data is null");
        }
        try {
            T message = objenesis.newInstance(cls);
            Schema<T> schema = getSchema(cls);
            ProtostuffIOUtil.mergeFrom(data,message,schema);
            return message;
        }catch (Exception e) {
            throw new SerializerException(e.getMessage(), e);
        }

    }
}
