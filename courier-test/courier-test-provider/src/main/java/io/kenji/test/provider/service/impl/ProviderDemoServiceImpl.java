package io.kenji.test.provider.service.impl;

import io.kenji.courier.annotation.RpcProvider;
import io.kenji.courier.test.api.DemoService;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/23
 **/
@Slf4j
@RpcProvider(interfaceClass = DemoService.class, interfaceName = "io.kenji.courier.test.api.DemoService", version = "1.0.0", group = "Kenji")
public class ProviderDemoServiceImpl implements DemoService {
    @Override
    public String hello(String name) {
        log.info("Invoked hello method, param: {}", name);
        return "Hello " + name;
    }
}
