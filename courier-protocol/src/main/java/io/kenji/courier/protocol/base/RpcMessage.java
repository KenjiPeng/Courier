package io.kenji.courier.protocol.base;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/23
 **/
@SuperBuilder
public class RpcMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = -4492154800128245592L;
    /**
     * Whether to send one-way
     */
    private boolean oneway;
    /**
     * Asynchronous call or not
     */
    private boolean async;
}
