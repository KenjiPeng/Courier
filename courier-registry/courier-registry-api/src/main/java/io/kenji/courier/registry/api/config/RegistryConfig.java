package io.kenji.courier.registry.api.config;

import io.kenji.courier.annotation.RegisterType;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-17
 **/
public record RegistryConfig(String registryAddr, RegisterType registerType) implements Serializable {
    @Serial
    private static final long serialVersionUID = 2024354595430621245L;
}
