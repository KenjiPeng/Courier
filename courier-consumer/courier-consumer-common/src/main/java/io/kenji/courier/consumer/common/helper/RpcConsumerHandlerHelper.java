package io.kenji.courier.consumer.common.helper;

import io.kenji.courier.consumer.common.handler.RpcConsumerHandler;
import io.kenji.courier.protocol.meta.ServiceMeta;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-18
 **/
public class RpcConsumerHandlerHelper {

    private static final Map<String, RpcConsumerHandler> rpcConsumerHandlerMap = new ConcurrentHashMap<>();

    private static String getKey(ServiceMeta key){
        return key.serviceAddr().concat("_").concat(String.valueOf(key.servicePort()));
    }

    public static void put(ServiceMeta key,RpcConsumerHandler value){
        rpcConsumerHandlerMap.put(getKey(key),value);
    }

    public static RpcConsumerHandler get(ServiceMeta key){
        return rpcConsumerHandlerMap.get(getKey(key));
    }

    public static void closeConsumerHandler(){
        Collection<RpcConsumerHandler> rpcConsumerHandlers = rpcConsumerHandlerMap.values();
        if (rpcConsumerHandlers.size()>0){
            rpcConsumerHandlers.forEach(RpcConsumerHandler::close);
        }
        rpcConsumerHandlerMap.clear();
    }

}
