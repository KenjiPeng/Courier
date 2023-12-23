package io.kenji.courier.serialization.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import io.kenji.courier.common.exception.SerializerException;
import io.kenji.courier.serialization.api.Serialization;
import io.kenji.courier.spi.annotation.SPIClass;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-22
 **/
@Slf4j
@SPIClass
public class KryoSerialization implements Serialization {
    private final Kryo kryo;

    public KryoSerialization() {
        kryo = new Kryo();
        kryo.setReferences(false);
    }

    @Override
    public <T> byte[] serialize(T obj) {
        log.info("Execute kryo serialize...");
        if (obj == null) {
            throw new SerializerException("serialize object is null");
        }
        kryo.register(obj.getClass(), new JavaSerializer());
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        Output output = new Output(bao);
        kryo.writeClassAndObject(output, obj);
        output.flush();
        output.close();
        byte[] bytes = bao.toByteArray();
        try {
            bao.flush();
            bao.close();
        } catch (IOException e) {
            throw new SerializerException(e.getMessage(), e);
        }
        return bytes;
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> cls) {
        log.info("Execute kryo deserialize...");
        if (data == null) {
            throw new SerializerException("deserialize data is null");
        }
        kryo.register(cls, new JavaSerializer());
        Input input = new Input(new ByteArrayInputStream(data));
        return (T) kryo.readClassAndObject(input);
    }
}
