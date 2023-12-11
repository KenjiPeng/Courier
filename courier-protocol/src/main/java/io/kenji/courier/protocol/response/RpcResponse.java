package io.kenji.courier.protocol.response;

import io.kenji.courier.protocol.base.RpcMessage;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serial;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/23
 **/
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
public class RpcResponse extends RpcMessage {
    @Serial
    private static final long serialVersionUID = 1942913084314941315L;

    private String error;

    private Object result;
}
