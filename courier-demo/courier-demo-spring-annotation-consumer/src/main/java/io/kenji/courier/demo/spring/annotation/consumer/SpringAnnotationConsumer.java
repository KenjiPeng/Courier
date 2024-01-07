package io.kenji.courier.demo.spring.annotation.consumer;

import io.kenji.courier.demo.spring.annotation.consumer.config.SpringAnnotationConsumerConfig;
import io.kenji.courier.demo.spring.annotation.consumer.service.ConsumerDemoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2024-01-07
 **/
@Slf4j
public class SpringAnnotationConsumer {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringAnnotationConsumerConfig.class);
        ConsumerDemoService demoService = context.getBean(ConsumerDemoService.class);
        String result = demoService.hello("Kenji :)");
        log.info("Got result: {}", result);
    }
}
