package io.kenji.courier.spring.boot.provider.starter;

import io.kenji.courier.provider.spring.RpcSpringServer;
import io.kenji.courier.spring.boot.provider.config.SpringBootProviderConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2024-01-08
 **/
@Configuration
public class SpringBootProviderAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "courier.provider")
    public SpringBootProviderConfig springBootProviderConfig() {
        return new SpringBootProviderConfig();
    }

    @Bean
    public RpcSpringServer rpcSpringServer(final SpringBootProviderConfig springBootProviderConfig) {
        return new RpcSpringServer(springBootProviderConfig.getServerAddress(), springBootProviderConfig.getRegistryAddress(), springBootProviderConfig.getRegisterType(),
                springBootProviderConfig.getReflectType(), springBootProviderConfig.getHeartbeatInterval(), springBootProviderConfig.getHeartbeatIntervalTimeUnit(),
                springBootProviderConfig.getScanNotActiveChannelInterval(), springBootProviderConfig.getScanNotActiveChannelIntervalTimeUnit());
    }
}
