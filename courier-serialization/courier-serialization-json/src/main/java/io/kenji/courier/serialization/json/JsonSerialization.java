package io.kenji.courier.serialization.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.kenji.courier.common.exception.SerializerException;
import io.kenji.courier.serialization.api.Serialization;
import io.kenji.courier.spi.annotation.SPIClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-21
 **/
@SPIClass
@Slf4j
public class JsonSerialization implements Serialization {

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        objectMapper.setDateFormat(simpleDateFormat);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        objectMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT, false);
        objectMapper.disable(SerializationFeature.CLOSE_CLOSEABLE);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);
    }

    @Override
    public <T> byte[] serialize(T obj) {
        log.info("Execute json serialize...");
        if (obj == null) {
            throw new SerializerException("serialize object is null");
        }
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            throw new SerializerException(e.getMessage(), e);
        }
        return bytes;
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> cls) {
        log.info("Execute json deserialize...");
        if (data == null) {
            throw new SerializerException("deserialize data is null");
        }
        T obj;
        try {
            obj = objectMapper.readValue(data, cls);
        } catch (IOException e) {
            throw new SerializerException(e.getMessage(), e);
        }
        return obj;
    }
}
