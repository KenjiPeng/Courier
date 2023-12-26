package io.kenji.courier.loadbalancer.hash.weight.enhance;

import io.kenji.courier.loadbalancer.api.base.BaseEnhanceServiceLoadBalancer;
import io.kenji.courier.protocol.meta.ServiceMeta;
import io.kenji.courier.spi.annotation.SPIClass;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-26
 **/
@Slf4j
@SPIClass
public class HashWeightServiceEnhanceLoadBalancer extends BaseEnhanceServiceLoadBalancer {

    @Override
    public ServiceMeta select(List<ServiceMeta> servers, int hashCode, String sourceIp) {
        log.info("Select server base on enhance hash weight load balance");
        if (servers == null || servers.size() == 0) {
            return null;
        }
        servers = this.getWeightServiceMetaList(servers);
        int index = Math.abs(hashCode) % servers.size();
        return servers.get(index);
    }
}
