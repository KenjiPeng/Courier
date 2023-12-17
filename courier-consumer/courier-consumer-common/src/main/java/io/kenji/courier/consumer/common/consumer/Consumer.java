package io.kenji.courier.consumer.common.consumer;

import io.kenji.courier.consumer.common.future.RpcFuture;
import io.kenji.courier.protocol.RpcProtocol;
import io.kenji.courier.protocol.request.RpcRequest;

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
