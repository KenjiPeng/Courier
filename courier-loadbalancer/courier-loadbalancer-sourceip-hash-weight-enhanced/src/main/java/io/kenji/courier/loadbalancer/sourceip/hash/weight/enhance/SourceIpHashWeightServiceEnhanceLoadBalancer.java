package io.kenji.courier.loadbalancer.sourceip.hash.weight.enhance;

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
public class SourceIpHashWeightServiceEnhanceLoadBalancer extends BaseEnhanceServiceLoadBalancer {
    @Override
    public ServiceMeta select(List<ServiceMeta> servers, int hashCode, String sourceIp) {
        log.info("Select server base on source Ip hash weight enhance load balance");
        if (servers == null || servers.size() == 0) {
            return null;
        }
        servers = this.getWeightServiceMetaList(servers);
        int resultHashCode = Math.abs(hashCode + sourceIp.hashCode());
        return servers.get(resultHashCode % servers.size());
    }
}
