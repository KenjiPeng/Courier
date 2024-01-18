package io.kenji.courier.demo.docker.consumer.service.impl;

import io.kenji.courier.annotation.ProxyType;
import io.kenji.courier.annotation.RegisterType;
import io.kenji.courier.annotation.RpcConsumer;
import io.kenji.courier.annotation.SerializationType;
import io.kenji.courier.constants.RegistryLoadBalanceType;
import io.kenji.courier.demo.api.DemoService;
import io.kenji.courier.demo.docker.consumer.service.ConsumerDemoService;
import org.springframework.stereotype.Service;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2024-01-10
 **/
@Service
public class ConsumerDemoServiceImpl implements ConsumerDemoService {

    @RpcConsumer(registerType = RegisterType.ZOOKEEPER,registerAddress = "192.168.3.37:2181",loadBalanceType = RegistryLoadBalanceType.LEAST_CONNECTION_ENHANCE,
    version = "1.0.0",group = "Kenji",serializationType = SerializationType.HESSIAN2,proxyType = ProxyType.BYTE_BUDDY,requestTimeoutInMilliseconds = 60000,
    async = false,oneway = false)
    private DemoService demoService;

    @Override
    public String hello(String name) {
        return demoService.hello(name);
    }
}
