package io.kenji.courier.demo.spring.annotation.provider;

import io.kenji.courier.demo.spring.annotation.provider.config.SpringAnnotationProviderConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2024-01-06
 **/
public class SpringAnnotationProviderStarter {

    public static void main(String[] args) {
        new AnnotationConfigApplicationContext(SpringAnnotationProviderConfig.class);
    }
}
