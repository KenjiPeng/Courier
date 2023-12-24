package io.kenji.courier.loadbalancer.round.robin.weight;

import io.kenji.courier.loadbalancer.api.ServiceLoadBalancer;
import io.kenji.courier.spi.annotation.SPIClass;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-24
 **/
@Slf4j
@SPIClass
public class RobinWeightServiceLoadBalancer<T> implements ServiceLoadBalancer<T> {

    private volatile AtomicInteger atomicInteger = new AtomicInteger(0);

    @Override
    public T select(List<T> servers, int hashCode, String sourceIp) {
        log.info("Select server base on round robin weight load balance");
        if (servers == null || servers.size() == 0) {
            return null;
        }
        hashCode = Math.abs(hashCode);
        int count = hashCode % servers.size();
        if (count <= 0) {
            count = servers.size();
        }
        int index = atomicInteger.getAndIncrement();
        if (index >= Integer.MAX_VALUE - 10000) {
            atomicInteger.set(0);
        }
        return servers.get(index % count);
    }
}
