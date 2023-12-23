package io.kenji.courier.serialization.fst;

import io.kenji.courier.common.exception.SerializerException;
import io.kenji.courier.serialization.api.Serialization;
import io.kenji.courier.spi.annotation.SPIClass;
import lombok.extern.slf4j.Slf4j;
import org.nustaq.serialization.FSTConfiguration;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-22
 **/
@SPIClass
@Slf4j
public class FstSerialization implements Serialization {
    private final FSTConfiguration configuration = FSTConfiguration.getDefaultConfiguration();

    @Override
    public <T> byte[] serialize(T obj) {
        log.info("Execute fst serialize...");
        if (obj == null) {
            throw new SerializerException("serialize object is null");
        }
        return configuration.asByteArray(obj);
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> cls) {
        log.info("Execute fst deserialize...");
        if (data == null) {
            throw new SerializerException("deserialize data is null");
        }
        return (T) configuration.asObject(data);
    }
}
