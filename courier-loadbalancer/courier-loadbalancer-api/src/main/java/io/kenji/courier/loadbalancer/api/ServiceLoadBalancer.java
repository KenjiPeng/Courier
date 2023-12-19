package io.kenji.courier.loadbalancer.api;

import java.util.List;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-19
 **/
public interface ServiceLoadBalancer<T> {


    T select(List<T> servers,int hashCode);

}
