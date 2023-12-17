package io.kenji.courier.test.consumer;

import io.kenji.courier.annotation.SerializationType;
import io.kenji.courier.consumer.RpcClient;
import io.kenji.courier.test.api.DemoService;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-17
 **/
@Slf4j
public class RpcConsumerNativeTest {

    public static void main(String[] args) {
        RpcClient<Object> rpcClient = new RpcClient<>("1.0.0", "Kenji", 3000, SerializationType.JDK, false, false);
        DemoService demoService = rpcClient.create(DemoService.class);
        String result = demoService.hello("Kenji");
        log.info("Got result: {}",result);
        rpcClient.shutdown();
    }
}