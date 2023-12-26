package io.kenji.courier.loadbalancer.consistenthash.enhance;

import io.kenji.courier.loadbalancer.api.base.BaseEnhanceServiceLoadBalancer;
import io.kenji.courier.protocol.meta.ServiceMeta;
import io.kenji.courier.spi.annotation.SPIClass;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-26
 **/
@Slf4j
@SPIClass
public class ZKConsistentHashEnhanceLoadBalancer extends BaseEnhanceServiceLoadBalancer {

    private final static int VIRTUAL_NODE_SIZE = 10;
    private final static String VIRTUAL_NODE_SPLIT = "#";

    @Override
    public ServiceMeta select(List<ServiceMeta> servers, int hashCode, String sourceIp) {
        log.info("Select server base on Zookeeper consistent hash load balance");
        TreeMap<Integer, ServiceMeta> ring = makeConsistentHashRing(this.getWeightServiceMetaList(servers));
        return allocateNode(ring, hashCode);
    }

    private ServiceMeta allocateNode(TreeMap<Integer, ServiceMeta> ring, int hashCode) {
        Map.Entry<Integer, ServiceMeta> entry = ring.ceilingEntry(hashCode);
        if (entry == null) {
            entry = ring.firstEntry();
        }
        if (entry == null) {
            throw new RuntimeException("Can not discover available service provider, please register service provider in registry center");
        }
        return entry.getValue();
    }


    private TreeMap<Integer, ServiceMeta> makeConsistentHashRing(List<ServiceMeta> servers) {
        TreeMap<Integer, ServiceMeta> ring = new TreeMap<>();
        for (ServiceMeta instance : servers) {
            for (int i = 0; i < VIRTUAL_NODE_SIZE; i++) {
                ring.put((buildServiceInstanceKey(instance) + VIRTUAL_NODE_SPLIT + i).hashCode(), instance);
            }
        }
        return ring;
    }

    private String buildServiceInstanceKey(ServiceMeta instance) {
        return String.join(":", instance.serviceAddr(), String.valueOf(instance.servicePort()));
    }
}
