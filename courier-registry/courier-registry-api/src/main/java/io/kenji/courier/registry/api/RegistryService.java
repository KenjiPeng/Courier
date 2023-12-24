package io.kenji.courier.registry.api;

import io.kenji.courier.protocol.meta.ServiceMeta;
import io.kenji.courier.registry.api.config.RegistryConfig;

import java.io.IOException;
import java.util.Optional;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-17
 **/
public interface RegistryService {

    void register(ServiceMeta serviceMeta) throws Exception;

    void unregister(ServiceMeta serviceMeta) throws Exception;

    Optional<ServiceMeta> discovery(String serviceName, int invokerHashCode, String sourceIp) throws Exception;

    void destroy() throws IOException;

    default void init(RegistryConfig registryConfig) throws Exception {
    }

}
