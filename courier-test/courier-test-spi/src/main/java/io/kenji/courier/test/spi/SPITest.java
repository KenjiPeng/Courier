package io.kenji.courier.test.spi;

import io.kenji.courier.spi.loader.ExtensionLoader;
import io.kenji.courier.test.spi.service.SPIService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-19
 **/
@Slf4j
public class SPITest {

    @Test
    public void testSpiLoader() {
        SPIService spiService = ExtensionLoader.getExtension(SPIService.class, "spiService");
        String result = spiService.hello("kenji");
        log.info(result);
    }
}
