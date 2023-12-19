package io.kenji.courier.test.spi.service.impl;

import io.kenji.courier.spi.annotation.SPIClass;
import io.kenji.courier.test.spi.service.SPIService;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-19
 **/
@SPIClass
public class SPIServiceImpl implements SPIService {
    @Override
    public String hello(String name) {
        return "hello " + name + " SPI";
    }
}
