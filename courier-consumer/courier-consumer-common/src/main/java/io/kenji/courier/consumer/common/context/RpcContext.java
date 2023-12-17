package io.kenji.courier.consumer.common.context;


import io.kenji.courier.consumer.common.future.RpcFuture;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-16
 **/
public class RpcContext {
    private RpcContext(){}

    private static final RpcContext AGENT = new RpcContext();
    /**
     * For saving RpcFuture
     */
    private static final InheritableThreadLocal<RpcFuture> RPC_FUTURE_INHERITABLE_THREAD_LOCAL = new InheritableThreadLocal<>();

    public static RpcContext getContext(){
        return AGENT;
    }

    /**
     * Save RpcFuture into InheritableThreadLocal
     * @param rpcFuture
     */
    public void setRpcFuture(RpcFuture rpcFuture){
        RPC_FUTURE_INHERITABLE_THREAD_LOCAL.set(rpcFuture);
    }

    /**
     * Get RpcFuture from InheritableThreadLocal
     * @return
     */
    public RpcFuture getRpcFuture(){
        return RPC_FUTURE_INHERITABLE_THREAD_LOCAL.get();
    }

    /**
     * Remove RpcFuture from InheritableThreadLocal
     */
    public void removeRpcFuture(){
        RPC_FUTURE_INHERITABLE_THREAD_LOCAL.remove();
    }
}
