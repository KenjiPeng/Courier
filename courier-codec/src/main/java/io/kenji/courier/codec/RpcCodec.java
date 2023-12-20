package io.kenji.courier.codec;

import io.kenji.courier.serialization.api.Serialization;
import io.kenji.courier.spi.loader.ExtensionLoader;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/26
 **/
public interface RpcCodec {
    default Serialization getSerialization(String serializationType) {
        return ExtensionLoader.getExtension(Serialization.class, serializationType);
    }
}
