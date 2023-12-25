package io.kenji.courier.loadbalancer.random.weight.enhance;

import io.kenji.courier.loadbalancer.api.base.BaseEnhanceServiceLoadBalancer;
import io.kenji.courier.protocol.meta.ServiceMeta;
import io.kenji.courier.spi.annotation.SPIClass;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-25
 **/
@SPIClass
@Slf4j
public class RandomWeightServiceEnhanceLoadBalancer extends BaseEnhanceServiceLoadBalancer {
    @Override
    public ServiceMeta select(List<ServiceMeta> servers, int hashCode, String sourceIp) {
        log.info("Select server base on enhance random weight load balance");
        if (servers == null || servers.size() == 0) {
            return null;
        }
        servers = this.getWeightServiceMetaList(servers);
        Random random = new Random();
        int index = random.nextInt(servers.size());
        return servers.get(index);
    }
}
