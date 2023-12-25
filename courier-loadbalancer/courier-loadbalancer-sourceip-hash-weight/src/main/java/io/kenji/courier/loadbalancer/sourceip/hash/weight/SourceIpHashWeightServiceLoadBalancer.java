package io.kenji.courier.loadbalancer.sourceip.hash.weight;

import io.kenji.courier.loadbalancer.api.ServiceLoadBalancer;
import io.kenji.courier.spi.annotation.SPIClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-25
 **/
@Slf4j
@SPIClass
public class SourceIpHashWeightServiceLoadBalancer<T> implements ServiceLoadBalancer<T> {

    @Override
    public T select(List<T> servers, int hashCode, String sourceIp) {
        log.info("Select server base on source ip hash weight load balance");
        if (servers == null || servers.size() == 0) {
            return null;
        }
        if (StringUtils.isBlank(sourceIp)) {
            return servers.get(0);
        }
        int count = Math.abs(hashCode) % servers.size();
        if (count <= 0) {
            return servers.get(0);
        }
        int resultHashCode = Math.abs(sourceIp.hashCode() + hashCode);
        return servers.get(resultHashCode % count);
    }
}
