package io.kenji.courier.consumer.common.callback;

import io.kenji.courier.protocol.response.RpcResponse;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-16
 **/
public interface AsyncRpcCallback {
    /**
     * call back for success
     * @param response
     */
    void onSuccess(RpcResponse response);

    /**
     * call back for exception
     * @param e
     */
    void onException(Exception e);
}
