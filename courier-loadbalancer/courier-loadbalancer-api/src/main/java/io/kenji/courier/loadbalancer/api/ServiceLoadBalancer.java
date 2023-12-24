package io.kenji.courier.loadbalancer.api;

import io.kenji.courier.constants.RpcConstants;
import io.kenji.courier.spi.annotation.SPI;

import java.util.List;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-19
 **/
@SPI(RpcConstants.SERVICE_LOAD_BALANCER_RANDOM)
public interface ServiceLoadBalancer<T> {


    T select(List<T> servers, int hashCode, String sourceIp);

}
