package io.kenji.courier.test.consumer;

import io.kenji.courier.annotation.Proxy;
import io.kenji.courier.annotation.RegisterType;
import io.kenji.courier.annotation.SerializationType;
import io.kenji.courier.constants.RegistryLoadBalanceType;
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
        RpcClient<Object> rpcClient = new RpcClient<>("127.0.0.1:2181", RegisterType.ZOOKEEPER, RegistryLoadBalanceType.ENHANCE_RANDOM_WEIGHT, "1.0.0",
                "Kenji", 100000, SerializationType.PROTOSTUFF, Proxy.ASM, false, false);
        DemoService demoService = rpcClient.create(DemoService.class);
        String result = demoService.hello("Kenji");
        log.info("Got result: {}", result);
        rpcClient.shutdown();
    }

//    @Test
//    public void testAsyncInterfaceRpc() throws Exception {
//        RpcClient<Object> rpcClient = new RpcClient<>("1.0.0", "Kenji", 3000, SerializationType.JDK, true, false);
//        IAsyncObjectProxy demoService = rpcClient.createAsync(DemoService.class);
//        RpcFuture rpcFuture = demoService.call("hello", "Kenji haha");
//        Object result = rpcFuture.get().getResult();
//        log.info("Got result: {}",result);
//        rpcClient.shutdown();
//    }
}
