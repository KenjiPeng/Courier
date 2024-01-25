package io.kenji.courier.demo.spring.annotation.provider.config;

import io.kenji.courier.annotation.ReflectType;
import io.kenji.courier.annotation.RegisterType;
import io.kenji.courier.provider.spring.RpcSpringServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.concurrent.TimeUnit;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2024-01-06
 **/
@Configuration
@ComponentScan(value = {"io.kenji.courier.demo"})
@PropertySource(value = {"classpath:courier.properties"})
public class SpringAnnotationProviderConfig {

    @Value("${registry.address}")
    String registryAddress;

    @Value("${registry.type}")
    RegisterType registerType;

    @Value("${server.address}")
    String serverAddress;

    @Value("${reflect.type}")
    ReflectType reflectType;

    @Value("${server.heartbeatInterval}")
    int heartbeatInterval;

    @Value("${server.heartbeatInterval.timeUnit}")
    TimeUnit heartbeatIntervalTimeUnit;

    @Value("${server.scanNotActiveChannelInterval}")
    int scanNotActiveChannelInterval;

    @Value("${server.scanNotActiveChannelInterval.timeUnit}")
    TimeUnit scanNotActiveChannelIntervalTimeUnit;
    @Value("${server.cache.expireTime}")
    int resultCacheExpire;
    @Value("${server.result.cache.enable}")
    boolean enableResultCache;

    @Value("${server.threadPool.corePoolSize}")
    int corePoolSize;

    @Value("${server.threadPool.maximumPoolSize}")
    int maximumPoolSize;

    @Bean
    public RpcSpringServer rpcSpringServer() {
        return new RpcSpringServer(serverAddress, registryAddress, registerType, reflectType, heartbeatInterval, heartbeatIntervalTimeUnit,
                scanNotActiveChannelInterval, scanNotActiveChannelIntervalTimeUnit, resultCacheExpire, corePoolSize, maximumPoolSize, enableResultCache);
    }
}
