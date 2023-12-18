package io.kenji.courier.proxy.api.config;

import io.kenji.courier.annotation.SerializationType;
import io.kenji.courier.consumer.common.consumer.Consumer;
import io.kenji.courier.registry.api.RegistryService;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-17
 **/

public record ProxyConfig<T>(Class<T> clazz,
                             String serviceVersion,
                             String serviceGroup,
                             long timeout,
                             Consumer consumer,
                             SerializationType serializationType,
                             Boolean async,
                             Boolean oneway,
                             RegistryService registryService) implements Serializable {
    @Serial
    private static final long serialVersionUID = 8418203627690503695L;

}
