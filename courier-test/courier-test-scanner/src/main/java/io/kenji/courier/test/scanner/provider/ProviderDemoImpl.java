package io.kenji.courier.test.scanner.provider;

import io.kenji.courier.annotation.RpcProvider;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/9
 **/
@RpcProvider(interfaceClass = DemoProvider.class,interfaceName = "io.kenji.courier.test.scanner.provider.DemoProvider",version = "1.0.0",group = "kenji",weight = 2)
public class ProviderDemoImpl implements DemoProvider{
}
