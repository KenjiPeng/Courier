package io.kenji.courier.common.exception;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2024-01-21
 **/
public class RpcException extends RuntimeException{
    public RpcException() {
        super();
    }

    public RpcException(String message) {
        super(message);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }
}
