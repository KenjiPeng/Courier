package io.kenji.courier.serialization.jdk;

import io.kenji.courier.common.exception.SerializerException;
import io.kenji.courier.serialization.api.Serialization;

import java.io.*;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/26
 **/
public class JdkSerialization implements Serialization {
    @Override
    public <T> byte[] serialize(T obj) {
        if (obj == null) throw new SerializerException("Serialize object is null");
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(obj);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new SerializerException(e.getMessage(), e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> cls) {
        if (data == null) throw new SerializerException("Deserialize data is null");
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bis);
            return (T) ois.readObject();
        } catch (Exception e) {
            throw new SerializerException(e.getMessage(), e);
        }
    }
}
