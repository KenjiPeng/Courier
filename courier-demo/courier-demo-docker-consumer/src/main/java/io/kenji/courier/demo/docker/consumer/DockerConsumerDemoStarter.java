package io.kenji.courier.demo.docker.consumer;

import io.kenji.courier.demo.docker.consumer.service.ConsumerDemoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2024-01-10
 **/
@SpringBootApplication
@ComponentScan(basePackages = {"io.kenji.courier"})
@Slf4j
public class DockerConsumerDemoStarter {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(DockerConsumerDemoStarter.class, args);
        ConsumerDemoService consumerDemoService = applicationContext.getBean(ConsumerDemoService.class);
        String result = consumerDemoService.hello("Kenji ^_^");
        log.info("Got result: {}", result);
    }
}
