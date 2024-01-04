package io.kenji.io.kenji.courier.provider.spring;

import io.kenji.courier.annotation.ReflectType;
import io.kenji.courier.annotation.RegisterType;
import io.kenji.courier.annotation.RpcProvider;
import io.kenji.courier.common.helper.RpcServiceHelper;
import io.kenji.courier.protocol.meta.ServiceMeta;
import io.kenji.courier.provider.common.server.base.BaseServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.kenji.courier.constants.RpcConstants.SERVICE_PROVIDER_WEIGHT_MAX;
import static io.kenji.courier.constants.RpcConstants.SERVICE_PROVIDER_WEIGHT_MIN;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2024-01-05
 **/
@Slf4j
public class RpcSpringServer extends BaseServer implements ApplicationContextInitializer, InitializingBean {


    public RpcSpringServer(String serverAddress, String registryAddress, RegisterType registerType, ReflectType reflectType, int heartbeatInterval, TimeUnit heartbeatIntervalTimeUnit, int scanNotActiveChannelInterval, TimeUnit scanNotActiveChannelIntervalTimeUnit) {
        super(serverAddress, registryAddress, registerType, reflectType, heartbeatInterval, heartbeatIntervalTimeUnit, scanNotActiveChannelInterval, scanNotActiveChannelIntervalTimeUnit);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.startNettyServer();
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcProvider.class);
        if (MapUtils.isNotEmpty(serviceBeanMap)) {
            for (Object serviceBean : serviceBeanMap.values()) {
                RpcProvider rpcProvider = serviceBean.getClass().getAnnotation(RpcProvider.class);
                ServiceMeta serviceMeta = new ServiceMeta(this.getServiceName(rpcProvider), rpcProvider.version(), host, port, rpcProvider.group(), getWeight(rpcProvider.weight()));
                handlerMap.put(RpcServiceHelper.buildServiceKey(serviceMeta.serviceName(), serviceMeta.serviceVersion(), serviceMeta.serviceGroup()), serviceBean);
                try {
                    registryService.register(serviceMeta);
                } catch (Exception e) {
                    log.error("Hit exception during Rpc server init spring", e);
                }
            }
        }
    }

    private int getWeight(int weight) {
        if (weight < SERVICE_PROVIDER_WEIGHT_MIN) {
            weight = SERVICE_PROVIDER_WEIGHT_MIN;
        } else if (weight > SERVICE_PROVIDER_WEIGHT_MAX) {
            weight = SERVICE_PROVIDER_WEIGHT_MAX;
        }
        return weight;
    }

    private String getServiceName(RpcProvider rpcProvider) {
        Class<?> interfaceClass = rpcProvider.interfaceClass();
        if (interfaceClass == void.class) {
            return rpcProvider.interfaceName();
        }
        String serviceName = interfaceClass.getName();
        if (StringUtils.isBlank(serviceName)) {
            return rpcProvider.interfaceName();
        }
        return serviceName;
    }
}
