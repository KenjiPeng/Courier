package io.kenji.courier.protocol.header;

import io.kenji.courier.annotation.SerializationType;
import io.kenji.courier.common.id.IdFactory;
import io.kenji.courier.constants.RpcConstants;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/23
 **/
public class RpcHeaderFactory {

    public static RpcHeader getRpcProtocolHeader(SerializationType serializationType, int msgType) {
        return RpcHeader.builder()
                .magic(RpcConstants.MAGIC)
                .requestId(IdFactory.getId())
                .msgType((byte) msgType)
                .status((byte) 0x1)
                .serializationType(serializationType)
                .build();
    }
}
