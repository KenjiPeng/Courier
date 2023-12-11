package io.kenji.courier.protocol.enumeration;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-11
 **/
public enum RpcStatus {
    SUCCESS(0),
    FAIL(1);
    private final int code;

    RpcStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
