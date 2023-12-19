package io.kenji.courier.test.spi.service;

import io.kenji.courier.spi.annotation.SPI;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-19
 **/
@SPI("spiService")
public interface SPIService {
    String hello(String name);
}
