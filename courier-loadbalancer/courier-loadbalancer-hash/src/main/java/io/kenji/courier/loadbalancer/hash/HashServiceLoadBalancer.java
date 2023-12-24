package io.kenji.courier.loadbalancer.hash;

import io.kenji.courier.loadbalancer.api.ServiceLoadBalancer;
import io.kenji.courier.spi.annotation.SPIClass;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-25
 **/
@SPIClass
@Slf4j
public class HashServiceLoadBalancer<T> implements ServiceLoadBalancer<T> {
    @Override
    public T select(List<T> servers, int hashCode, String sourceIp) {
        log.info("Select server base on hash load balance");
        if (servers == null || servers.size() == 0) {
            return null;
        }
        int index = Math.abs(hashCode) % servers.size();
        return servers.get(index);
    }
}
