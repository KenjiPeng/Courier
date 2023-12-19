package io.kenji.courier.spi.factory;

import io.kenji.courier.spi.annotation.SPI;
import io.kenji.courier.spi.annotation.SPIClass;
import io.kenji.courier.spi.loader.ExtensionLoader;

import java.util.Optional;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-19
 **/
@SPIClass
public class SpiExtensionFactory implements ExtensionFactory{
    @Override
    public <T> T getExtension(String key, Class<T> clazz) {
        return Optional.ofNullable(clazz)
                .filter(Class::isInterface)
                .filter(cls->cls.isAnnotationPresent(SPI.class))
                .map(ExtensionLoader::getExtensionLoader)
                .map(ExtensionLoader::getDefaultSpiClassInstance)
                .orElse(null);
    }
}
