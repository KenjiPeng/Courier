package io.kenji.courier.protocol.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/23
 **/
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class RpcMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = -4492154800128245592L;
    /**
     * Whether to send one-way
     */
    private Boolean oneway;
    /**
     * Asynchronous call or not
     */
    private Boolean async;
}
