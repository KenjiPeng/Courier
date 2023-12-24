package io.kenji.courier.loadbalancer.random.weight;

import io.kenji.courier.loadbalancer.api.ServiceLoadBalancer;
import io.kenji.courier.spi.annotation.SPIClass;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-24
 **/
@SPIClass
@Slf4j
public class RandomWeightServiceLoadBalancer<T> implements ServiceLoadBalancer<T> {

    @Override
    public T select(List<T> servers, int hashCode, String sourceIp) {
        log.info("Select server base on random weight load balance");
        if (servers == null || servers.size() == 0) {
            return null;
        }
        hashCode = Math.abs(hashCode);
        int count = hashCode % servers.size();
        if (count <= 1) {
            count = servers.size();
        }
        Random random = new Random();
        int index = random.nextInt(count);
        return servers.get(index);
    }
}
