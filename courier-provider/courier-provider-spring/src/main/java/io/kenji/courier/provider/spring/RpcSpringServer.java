package io.kenji.courier.provider.spring;

import io.kenji.courier.annotation.ReflectType;
import io.kenji.courier.annotation.RegisterType;
import io.kenji.courier.annotation.RpcProvider;
import io.kenji.courier.common.helper.RpcServiceHelper;
import io.kenji.courier.protocol.meta.ServiceMeta;
import io.kenji.courier.provider.common.server.base.BaseServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

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
public class RpcSpringServer extends BaseServer implements ApplicationContextAware, InitializingBean {


    public RpcSpringServer(String serverAddress, String registryAddress, RegisterType registerType, ReflectType reflectType, int heartbeatInterval, TimeUnit heartbeatIntervalTimeUnit,
                           int scanNotActiveChannelInterval, TimeUnit scanNotActiveChannelIntervalTimeUnit, int corePoolSize, int maximumPoolSize,
                           int resultCacheExpire, boolean enableResultCache) {
        super(serverAddress, registryAddress, registerType, reflectType, heartbeatInterval, heartbeatIntervalTimeUnit, scanNotActiveChannelInterval, scanNotActiveChannelIntervalTimeUnit, resultCacheExpire, corePoolSize, maximumPoolSize, enableResultCache);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcProvider.class);
        if (MapUtils.isNotEmpty(serviceBeanMap)) {
            for (Object serviceBean : serviceBeanMap.values()) {
                RpcProvider rpcProvider = serviceBean.getClass().getAnnotation(RpcProvider.class);
                ServiceMeta serviceMeta = new ServiceMeta(this.getServiceName(rpcProvider), rpcProvider.version(), host, port, rpcProvider.group(), getWeight(rpcProvider.weight()));
                handlerMap.put(RpcServiceHelper.buildServiceKey(serviceMeta.serviceName(), serviceMeta.serviceVersion(), serviceMeta.serviceGroup()), serviceBean);
                try {
                    log.info("register service metadata: {}", serviceMeta);
                    registryService.register(serviceMeta);
                } catch (Exception e) {
                    log.error("Hit exception during Rpc server init spring", e);
                }
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.startNettyServer();
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
