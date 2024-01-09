package io.kenji.courier.spring.boot.consumer.starter;

import io.kenji.courier.consumer.RpcClient;
import io.kenji.courier.spring.boot.consumer.config.SpringBootConsumerConfig;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2024-01-09
 **/
@Configuration
@EnableAutoConfiguration
public class SpringBootConsumerAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "courier.consumer")
    public SpringBootConsumerConfig springBootConsumerConfig(){
        return new SpringBootConsumerConfig();
    }

    @Bean
    public RpcClient rpcClient(final SpringBootConsumerConfig springBootConsumerConfig){
        return RpcClient.builder()
                .registryAddress(springBootConsumerConfig.getRegistryAddress())
                .registerType(springBootConsumerConfig.getRegisterType())
                .proxyType(springBootConsumerConfig.getProxyType())
                .serviceVersion(springBootConsumerConfig.getServiceVersion())
                .registryLoadBalanceType(springBootConsumerConfig.getRegistryLoadBalanceType())
                .serviceGroup(springBootConsumerConfig.getServiceGroup())
                .async(springBootConsumerConfig.getAsync())
                .oneway(springBootConsumerConfig.getOneway())
                .serializationType(springBootConsumerConfig.getSerializationType())
                .requestTimeoutInMilliseconds(springBootConsumerConfig.getRequestTimeoutInMilliseconds())
                .heartbeatInterval(springBootConsumerConfig.getHeartbeatInterval())
                .heartbeatIntervalTimeUnit(springBootConsumerConfig.getHeartbeatIntervalTimeUnit())
                .scanNotActiveChannelInterval(springBootConsumerConfig.getScanNotActiveChannelInterval())
                .scanNotActiveChannelIntervalTimeUnit(springBootConsumerConfig.getScanNotActiveChannelIntervalTimeUnit())
                .retryIntervalInMillisecond(springBootConsumerConfig.getRetryIntervalInMillisecond())
                .maxRetryTime(springBootConsumerConfig.getMaxRetryTime())
                .build();

    }

}
