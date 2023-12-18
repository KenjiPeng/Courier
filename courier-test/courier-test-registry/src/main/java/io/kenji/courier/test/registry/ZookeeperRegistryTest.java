package io.kenji.courier.test.registry;

import io.kenji.courier.annotation.RegisterType;
import io.kenji.courier.protocol.meta.ServiceMeta;
import io.kenji.courier.registry.api.RegistryService;
import io.kenji.courier.registry.api.config.RegistryConfig;
import io.kenji.courier.registry.zookeeper.ZookeeperRegistryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-17
 **/
@Slf4j
public class ZookeeperRegistryTest {

    private RegistryService registryService;

    private ServiceMeta serviceMeta;

    @BeforeEach
    public void init() throws Exception {
        RegistryConfig registryConfig = new RegistryConfig("127.0.0.1:2181", RegisterType.ZOOKEEPER);
        this.registryService = new ZookeeperRegistryService();
        this.registryService.init(registryConfig);
        this.serviceMeta = new ServiceMeta(ZookeeperRegistryTest.class.getName(), "1.0.0", "127.0.0.1", 8080, "Kenji");
    }

    @Test
    public void testRegister() throws Exception {
        this.registryService.register(serviceMeta);
        Thread.sleep(9999999);
    }

    @Test
    public void testUnregister() throws Exception {
        this.registryService.unregister(serviceMeta);
    }

    @Test
    public void testDiscovery() throws Exception {
        Optional<ServiceMeta> discovery = this.registryService.discovery(RegistryService.class.getName(), "Kenji".hashCode());
        discovery.ifPresent(meta -> log.info("discovered service: {}", meta));
    }

    @Test
    public void testDestroy() throws Exception {
         this.registryService.destroy();
    }

}
