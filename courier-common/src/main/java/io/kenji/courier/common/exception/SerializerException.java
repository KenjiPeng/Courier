package io.kenji.courier.common.exception;

import java.io.Serial;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/25
 **/
public class SerializerException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -8949062374919105789L;

    public SerializerException(String message) {
        super(message);
    }

    public SerializerException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerializerException(Throwable cause) {
        super(cause);
    }
}
