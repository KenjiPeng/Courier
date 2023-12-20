package io.kenji.courier.serialization.hessian2;

import com.alibaba.com.caucho.hessian.io.Hessian2Input;
import com.alibaba.com.caucho.hessian.io.Hessian2Output;
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
 * @Date 2023-12-21
 **/
@SPIClass
@Slf4j
public class Hessian2Serialization implements Serialization {
    @Override
    public <T> byte[] serialize(T obj) {
        log.info("Execute hessian2 serialize...");
        if (obj == null) {
            throw new SerializerException("serialize object is null");
        }
        byte[] bytes;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Hessian2Output hessian2Output = new Hessian2Output(byteArrayOutputStream);
        try {
            hessian2Output.startMessage();
            hessian2Output.writeObject(obj);
            hessian2Output.flush();
            hessian2Output.completeMessage();
            bytes = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new SerializerException(e.getMessage(), e);
        }finally {
            try {
                if (hessian2Output!=null){
                    hessian2Output.close();
                    byteArrayOutputStream.close();
                }
            } catch (IOException e) {
                throw new SerializerException(e.getMessage(), e);
            }
        }
        return bytes;
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> cls) {
        log.info("Execute hessian2 deserialize...");
        if (data == null) {
            throw new SerializerException("deserialize data is null");
        }
        T obj;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        Hessian2Input hessian2Input = new Hessian2Input(byteArrayInputStream);
        try {
            hessian2Input.startMessage();
            obj = (T) hessian2Input.readObject(cls);
            hessian2Input.completeMessage();
        } catch (IOException e) {
            throw new SerializerException(e.getMessage(), e);
        }finally {
            try {
                if (hessian2Input!=null){
                    hessian2Input.close();
                    byteArrayInputStream.close();
                }
            } catch (IOException e) {
                throw new SerializerException(e.getMessage(), e);
            }
        }
        return obj;
    }
}
