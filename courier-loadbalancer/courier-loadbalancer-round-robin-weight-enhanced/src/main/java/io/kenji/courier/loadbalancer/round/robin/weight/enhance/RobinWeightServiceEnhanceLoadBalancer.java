package io.kenji.courier.loadbalancer.round.robin.weight.enhance;

import io.kenji.courier.loadbalancer.api.base.BaseEnhanceServiceLoadBalancer;
import io.kenji.courier.protocol.meta.ServiceMeta;
import io.kenji.courier.spi.annotation.SPIClass;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-26
 **/
@Slf4j
@SPIClass
public class RobinWeightServiceEnhanceLoadBalancer extends BaseEnhanceServiceLoadBalancer {

    private volatile AtomicInteger atomicInteger = new AtomicInteger(0);

    @Override
    public ServiceMeta select(List<ServiceMeta> servers, int hashCode, String sourceIp) {
        log.info("Select server base on round robin weight enhance load balance");
        if (servers == null || servers.size() == 0) {
            return null;
        }
        servers = this.getWeightServiceMetaList(servers);
        int index = atomicInteger.getAndIncrement();
        if (atomicInteger.get() >= Integer.MAX_VALUE - 10000) {
            atomicInteger.set(0);
        }
        return servers.get(index % servers.size());
    }
}
