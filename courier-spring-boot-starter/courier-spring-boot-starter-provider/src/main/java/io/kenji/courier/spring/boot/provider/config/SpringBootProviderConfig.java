package io.kenji.courier.spring.boot.provider.config;

import io.kenji.courier.annotation.ReflectType;
import io.kenji.courier.annotation.RegisterType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.TimeUnit;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2024-01-08
 **/
@Data
@NoArgsConstructor
public final class SpringBootProviderConfig {

    String serverAddress;

    String registryAddress;

    RegisterType registerType;

    ReflectType reflectType;

    int heartbeatInterval;

    TimeUnit heartbeatIntervalTimeUnit;

    int scanNotActiveChannelInterval;

    TimeUnit scanNotActiveChannelIntervalTimeUnit;

    public SpringBootProviderConfig(String serverAddress, String registryAddress, RegisterType registerType, ReflectType reflectType,
                                    int heartbeatInterval, TimeUnit heartbeatIntervalTimeUnit, int scanNotActiveChannelInterval,
                                    TimeUnit scanNotActiveChannelIntervalTimeUnit) {
        this.serverAddress = serverAddress;
        this.registryAddress = registryAddress;
        this.registerType = registerType;
        this.reflectType = reflectType;
        if (heartbeatInterval>0){
            this.heartbeatInterval = heartbeatInterval;
        }
        this.heartbeatIntervalTimeUnit = heartbeatIntervalTimeUnit;
        if (scanNotActiveChannelInterval>0){
            this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
        }
        this.scanNotActiveChannelIntervalTimeUnit = scanNotActiveChannelIntervalTimeUnit;
    }
}
