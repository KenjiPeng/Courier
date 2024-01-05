package io.kenji.courier.demo.spring.xml.provider.impl;

import io.kenji.courier.annotation.RpcProvider;
import io.kenji.courier.demo.api.DemoService;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2024-01-05
 **/
@Slf4j
@RpcProvider(interfaceClass = DemoService.class, interfaceName = "io.kenji.courier.demo.api.DemoService", version = "1.0.0", group = "Kenji", weight = 2)
public class ProviderDemoServiceImpl implements DemoService {
    @Override
    public String hello(String name) {
        log.info("Invoked hello method, param: {}", name);
        return "Hello " + name;
    }
}
