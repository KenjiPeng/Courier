package io.kenji.courier.protocol.request;

import io.kenji.courier.protocol.base.RpcMessage;
import lombok.Builder;
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
public class RpcRequest extends RpcMessage {

    @Serial
    private static final long serialVersionUID = 2228390007350986700L;

    /**
     * class name
     */
    private String className;

    /**
     * method name
     */
    private String methodName;

    /**
     * param type array
     */
    private Class<?>[] parameterTypes;

    /**
     * param array
     */
    private Object[] parameters;

    /**
     * version
     */
    private String version;

    /**
     * service group
     */
    private String group;

}
