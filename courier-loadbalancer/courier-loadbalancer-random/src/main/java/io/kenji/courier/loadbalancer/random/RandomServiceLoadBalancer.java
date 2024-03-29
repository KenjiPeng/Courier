package io.kenji.courier.loadbalancer.random;

import io.kenji.courier.loadbalancer.api.ServiceLoadBalancer;
import io.kenji.courier.spi.annotation.SPIClass;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-19
 **/
@Slf4j
@SPIClass
public class RandomServiceLoadBalancer<T> implements ServiceLoadBalancer<T> {

    @Override
    public T select(List<T> servers, int hashCode, String sourceIp) {
        log.info("Select server base on random load balance");
        if (servers != null && !servers.isEmpty()) {
            Random random = new Random();
            int index = random.nextInt(servers.size());
            return servers.get(index);
        }
        log.warn("Can't select remote service provider...");
        return null;
    }
}
