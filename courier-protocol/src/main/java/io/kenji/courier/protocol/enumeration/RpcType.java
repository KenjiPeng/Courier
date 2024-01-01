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
     * HeartBeat request data from consumer
     */
    HEARTBEAT_REQUEST_FROM_CONSUMER(3),
    /**
     * HeartBeat response data from provider
     */
    HEARTBEAT_RESPONSE_FROM_PROVIDER(4),
    /**
     * HeartBeat request data from provider
     */
    HEARTBEAT_REQUEST_FROM_PROVIDER(5),
    /**
     * HeartBeat request data from consumer
     */
    HEARTBEAT_RESPONSE_FROM_CONSUMER(6);

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
