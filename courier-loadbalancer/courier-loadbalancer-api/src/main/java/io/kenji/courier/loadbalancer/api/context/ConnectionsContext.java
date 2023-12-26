package io.kenji.courier.loadbalancer.api.context;

import io.kenji.courier.protocol.meta.ServiceMeta;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-26
 **/
public class ConnectionsContext {


    private static volatile Map<String, Integer> connectionsMap = new ConcurrentHashMap<>();

    public static void add(ServiceMeta serviceMeta) {
        String key = generateKey(serviceMeta);
        Integer count = connectionsMap.get(key);
        if (count == null) {
            count = 0;
        }
        count++;
        connectionsMap.put(key, count);
    }

    public static Integer getConnectionCount(ServiceMeta serviceMeta) {
        String key = generateKey(serviceMeta);
        return connectionsMap.get(key);
    }

    private static String generateKey(ServiceMeta serviceMeta) {
        return serviceMeta.serviceAddr().concat(":").concat(String.valueOf(serviceMeta.servicePort()));
    }


}
