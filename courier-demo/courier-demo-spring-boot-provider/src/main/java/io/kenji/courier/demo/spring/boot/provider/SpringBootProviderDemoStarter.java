package io.kenji.courier.demo.spring.boot.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2024-01-08
 **/
@SpringBootApplication
@ComponentScan(value = {"io.kenji.courier"})
public class SpringBootProviderDemoStarter {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootProviderDemoStarter.class, args);
    }
}
