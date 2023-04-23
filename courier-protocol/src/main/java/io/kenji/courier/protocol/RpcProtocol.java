package io.kenji.courier.protocol;

import io.kenji.courier.protocol.header.RpcHeader;
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
public class RpcProtocol<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 3471592270664431185L;
    private RpcHeader header;
    private T body;
}
