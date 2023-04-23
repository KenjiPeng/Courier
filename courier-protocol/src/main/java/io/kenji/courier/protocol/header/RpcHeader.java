package io.kenji.courier.protocol.header;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/23
 **/
@Builder
public class RpcHeader implements Serializable {
    @Serial
    private static final long serialVersionUID = 8418203627690513685L;

    /**
     * magic 2 bytes
     */
    private short magic;

    /**
     * message Type 1 byte
     */
    private byte msgType;

    /**
     * status 1 byte
     */
    private byte status;

    /**
     * message ID 8 bytes
     */
    private long requestId;

    /**
     * serialization type 16 bytes. Less than 16 bytes followed by a 0, the convention is that the length of the serialized type cannot exceed 16
     */
    private String serializationType;

    /**
     * message length 4 bytes
     */
    private int msgLen;
}
