package io.kenji.courier.codec;

import io.kenji.courier.serialization.api.Serialization;
import io.kenji.courier.serialization.jdk.JdkSerialization;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/26
 **/
public interface RpcCodec {
    default Serialization gerJdkSerialization(){
        return new JdkSerialization();
    }
}
