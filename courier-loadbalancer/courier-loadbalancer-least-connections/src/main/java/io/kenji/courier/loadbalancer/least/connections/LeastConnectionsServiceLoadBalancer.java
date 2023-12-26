package io.kenji.courier.loadbalancer.least.connections;

import io.kenji.courier.loadbalancer.api.ServiceLoadBalancer;
import io.kenji.courier.loadbalancer.api.context.ConnectionsContext;
import io.kenji.courier.protocol.meta.ServiceMeta;
import io.kenji.courier.spi.annotation.SPIClass;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-26
 **/
@SPIClass
@Slf4j
public class LeastConnectionsServiceLoadBalancer implements ServiceLoadBalancer<ServiceMeta> {
    @Override
    public ServiceMeta select(List<ServiceMeta> servers, int hashCode, String sourceIp) {
        log.info("Select server base on least connections load balance");
        if (servers == null || servers.size() == 0) {
            return null;
        }
        ServiceMeta serviceMeta = getNullConnectedServiceMeta(servers);
        if (serviceMeta == null) {
            return getLeastConnectionServiceMeta(servers);
        }
        return serviceMeta;
    }

    private ServiceMeta getLeastConnectionServiceMeta(List<ServiceMeta> servers) {
        ServiceMeta serviceMeta = servers.get(0);
        Integer count = ConnectionsContext.getConnectionCount(serviceMeta);
        for (ServiceMeta meta : servers) {
            Integer metaCount = ConnectionsContext.getConnectionCount(meta);
            if (count > metaCount) {
                count = metaCount;
                serviceMeta = meta;
            }
        }
        return serviceMeta;
    }

    private ServiceMeta getNullConnectedServiceMeta(List<ServiceMeta> servers) {
        return servers.stream().filter(service -> ConnectionsContext.getConnectionCount(service) == null).findAny().orElse(null);
    }
}
