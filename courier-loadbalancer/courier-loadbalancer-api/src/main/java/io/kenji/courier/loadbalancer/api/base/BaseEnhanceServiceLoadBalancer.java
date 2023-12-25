package io.kenji.courier.loadbalancer.api.base;

import io.kenji.courier.loadbalancer.api.ServiceLoadBalancer;
import io.kenji.courier.protocol.meta.ServiceMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-25
 **/
public abstract class BaseEnhanceServiceLoadBalancer implements ServiceLoadBalancer<ServiceMeta> {

    protected List<ServiceMeta> getWeightServiceMetaList(List<ServiceMeta> servers) {
        if (servers == null || servers.size() == 0) {
            return null;
        }
        List<ServiceMeta> serviceMetaList = new ArrayList<>();
        for (ServiceMeta serviceMeta : servers) {
            IntStream.range(0, serviceMeta.weight()).forEach(i -> serviceMetaList.add(serviceMeta));
        }
        return serviceMetaList;
    }
}
