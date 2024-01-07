package io.kenji.courier.demo.spring.xml.consumer;

import io.kenji.courier.consumer.RpcClient;
import io.kenji.courier.demo.api.DemoService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2024-01-06
 **/
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringJUnitConfig
@ContextConfiguration(locations = "classpath:spring.xml")
public class SpringXmlConsumerTest {
    @Autowired
    private RpcClient rpcClient;


    @Test
    public void testInterfaceRpc() {
        DemoService demoService = rpcClient.create(DemoService.class);
        String result = demoService.hello("Kenji");
        log.info("Got result: {}", result);
    }
}
