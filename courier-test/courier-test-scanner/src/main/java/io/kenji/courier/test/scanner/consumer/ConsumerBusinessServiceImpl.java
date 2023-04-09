package io.kenji.courier.test.scanner.consumer;

import io.kenji.courier.annotation.RpcConsumer;
import io.kenji.courier.test.scanner.provider.DemoProvider;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/9
 **/
public class ConsumerBusinessServiceImpl implements ConsumerBusinessService {
    @RpcConsumer(registerType = "zookeeper", registerAddress = "127.0.0.1:2181", version = "1.0.0", group = "kenji")
    private DemoProvider demoProvider;
}
