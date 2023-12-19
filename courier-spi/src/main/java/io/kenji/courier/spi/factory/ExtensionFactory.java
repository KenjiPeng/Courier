package io.kenji.courier.spi.factory;

import io.kenji.courier.spi.annotation.SPI;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-19
 **/
@SPI("spi")
public interface ExtensionFactory {

    /**
     * Get extension instance
     * @param key
     * @param clazz
     * @return
     * @param <T>
     */
    <T> T getExtension(String key,Class<T> clazz);
}
