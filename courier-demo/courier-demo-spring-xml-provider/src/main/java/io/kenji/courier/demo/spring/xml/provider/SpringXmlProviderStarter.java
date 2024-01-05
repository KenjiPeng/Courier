package io.kenji.courier.demo.spring.xml.provider;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2024-01-05
 **/
public class SpringXmlProviderStarter {

    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("spring.xml");
    }
}
