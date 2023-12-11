package io.kenji.courier.protocol.enumeration;

import java.util.Arrays;
import java.util.Optional;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/23
 **/
public enum RpcType {
    /**
     * Request message
     */
    REQUEST(1),
    /**
     * Response message
     */
    RESPONSE(2),
    /**
     * HeartBeat data
     */
    HEARTBEAT(3);

    private final int type;

    RpcType(int type) {
        this.type = type;
    }

    public static Optional<RpcType> findByType(int type) {
        return Arrays.stream(RpcType.values()).filter(rpcT -> rpcT.type == type).findAny();
    }

    public int getType() {
        return type;
    }
}
