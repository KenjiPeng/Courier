package io.kenji.courier.common.utils;

import com.google.gson.*;
import lombok.SneakyThrows;

import java.lang.reflect.Type;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-11
 **/
public class GsonUtil {

    public static Gson getGson() {
        return new GsonBuilder().registerTypeAdapter(Class.class, new ClassCodec()).create();
    }

    private static class ClassCodec implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {

        @SneakyThrows
        @Override
        public Class<?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            String clazz = jsonElement.getAsString();
            return Class.forName(clazz);
        }

        @Override
        public JsonElement serialize(Class<?> aClass, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(aClass.getName());
        }
    }


}
