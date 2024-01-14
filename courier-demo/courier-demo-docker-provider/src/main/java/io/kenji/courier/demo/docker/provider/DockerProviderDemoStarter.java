package io.kenji.courier.demo.docker.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2024-01-14
 **/
@SpringBootApplication
@ComponentScan(value = {"io.kenji.courier"})
public class DockerProviderDemoStarter {

    public static void main(String[] args) {
        SpringApplication.run(DockerProviderDemoStarter.class, args);
    }
}
