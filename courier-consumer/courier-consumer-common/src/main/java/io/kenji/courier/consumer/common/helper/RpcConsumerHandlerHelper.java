package io.kenji.courier.consumer.common.helper;

import io.kenji.courier.common.helper.RpcConnectionHelper;
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

    public static void put(String serviceAddr, int servicePort, RpcConsumerHandler value) {
        rpcConsumerHandlerMap.put(RpcConnectionHelper.buildConnectionKey(serviceAddr, servicePort), value);
    }

    public static RpcConsumerHandler get(ServiceMeta key) {
        return rpcConsumerHandlerMap.get(RpcConnectionHelper.buildConnectionKey(key.serviceAddr(), key.servicePort()));
    }

    public static void remove(String serviceAddr, int servicePort){
        rpcConsumerHandlerMap.remove(RpcConnectionHelper.buildConnectionKey(serviceAddr, servicePort));
    }

    public static void closeConsumerHandler() {
        Collection<RpcConsumerHandler> rpcConsumerHandlers = rpcConsumerHandlerMap.values();
        if (rpcConsumerHandlers.size() > 0) {
            rpcConsumerHandlers.forEach(RpcConsumerHandler::close);
        }
        rpcConsumerHandlerMap.clear();
    }

}
