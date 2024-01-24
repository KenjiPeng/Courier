package io.kenji.courier.registry.api;

import io.kenji.courier.protocol.meta.ServiceMeta;
import io.kenji.courier.registry.api.config.RegistryConfig;
import io.kenji.courier.spi.annotation.SPI;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-17
 **/
@SPI
public interface RegistryService {

    void register(ServiceMeta serviceMeta) throws Exception;

    void unregister(ServiceMeta serviceMeta) throws Exception;

    Optional<ServiceMeta> discovery(String serviceName, int invokerHashCode, String sourceIp) throws Exception;

    void destroy() throws IOException;

    Optional<ServiceMeta> select(List<ServiceMeta> serviceMetaList, int invokerHashCode, String sourceIp);

    List<ServiceMeta> discoveryAll() throws Exception;

    default void init(RegistryConfig registryConfig) throws Exception {
    }

}
