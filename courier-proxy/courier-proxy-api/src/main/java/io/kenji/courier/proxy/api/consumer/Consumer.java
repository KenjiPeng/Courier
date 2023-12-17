package io.kenji.courier.proxy.api.consumer;

import io.kenji.courier.protocol.RpcProtocol;
import io.kenji.courier.protocol.request.RpcRequest;
import io.kenji.courier.proxy.api.future.RpcFuture;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-17
 **/
public interface Consumer {

    /**
     * Rpc consumer sends request
     * @param requestRpcProtocol
     * @return
     * @throws Exception
     */
    RpcFuture sendRequest(RpcProtocol<RpcRequest> requestRpcProtocol) throws Exception;
}
