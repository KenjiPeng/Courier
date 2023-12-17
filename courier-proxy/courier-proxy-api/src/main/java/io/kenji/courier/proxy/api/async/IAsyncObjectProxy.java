package io.kenji.courier.proxy.api.async;


import io.kenji.courier.consumer.common.future.RpcFuture;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-17
 **/
public interface IAsyncObjectProxy {
    /**
     * The invoke method in async object
     * @param funcName
     * @param args
     * @return
     */
    RpcFuture call(String funcName, Object... args);
}
