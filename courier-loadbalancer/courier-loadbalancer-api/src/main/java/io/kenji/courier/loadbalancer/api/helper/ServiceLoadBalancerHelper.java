package io.kenji.courier.loadbalancer.api.helper;

import io.kenji.courier.protocol.meta.ServiceMeta;
import org.apache.curator.x.discovery.ServiceInstance;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-25
 **/
public class ServiceLoadBalancerHelper {

    private static volatile List<ServiceMeta> cacheServiceMeta = new CopyOnWriteArrayList<>();

    public static List<ServiceMeta> getServiceMetaList(List<ServiceInstance<ServiceMeta>> serviceInstances) {

        if (serviceInstances == null || serviceInstances.size() == 0 || serviceInstances.size() == cacheServiceMeta.size()) {
            return cacheServiceMeta;
        }
        cacheServiceMeta.clear();
        serviceInstances.forEach(serviceMetaServiceInstance -> cacheServiceMeta.add(serviceMetaServiceInstance.getPayload()));
        return cacheServiceMeta;
    }

}
