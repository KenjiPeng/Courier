package io.kenji.test.provider.service.impl;

import io.kenji.courier.annotation.RpcProvider;
import io.kenji.test.provider.service.DemoService;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/23
 **/
@RpcProvider(interfaceClass = DemoService.class,interfaceName = "io.kenji.test.provider.service.DemoService",version = "1.0.0",group = "Kenji")
public class ProviderDemoServiceImpl implements DemoService {
}
