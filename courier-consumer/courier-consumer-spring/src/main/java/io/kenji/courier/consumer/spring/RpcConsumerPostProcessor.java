package io.kenji.courier.consumer.spring;

import io.kenji.courier.annotation.RpcConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2024-01-06
 **/
@Slf4j
@Component
public class RpcConsumerPostProcessor implements ApplicationContextAware, BeanClassLoaderAware, BeanFactoryPostProcessor {

    private ApplicationContext context;

    private ClassLoader classLoader;

    private final Map<String, BeanDefinition> rpcConsumerBeanDefinitions = new LinkedHashMap<>();

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        for (String beanDefinitionName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName);
            String beanClassName = beanDefinition.getBeanClassName();
            if (beanClassName != null) {
                Class<?> clazz = ClassUtils.resolveClassName(beanClassName, this.classLoader);
                ReflectionUtils.doWithFields(clazz, this::parseRpcConsumer);
            }
        }
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        this.rpcConsumerBeanDefinitions.forEach((beanName, beanDefinition) -> {
            if (context.containsBean(beanName)) {
                throw new IllegalArgumentException("Spring context already has a bean named " + beanName);
            }
            registry.registerBeanDefinition(beanName, rpcConsumerBeanDefinitions.get(beanName));
            log.info("Registered RpcConsumerBean {} success", beanName);
        });
    }

    private void parseRpcConsumer(Field field) {
        RpcConsumer annotation = AnnotationUtils.getAnnotation(field, RpcConsumer.class);
        if (annotation != null) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(RpcConsumerBean.class);
            builder.setInitMethodName("init");
            builder.addPropertyValue("interfaceClass", field.getType());
            builder.addPropertyValue("serviceVersion", annotation.version());
            builder.addPropertyValue("serviceGroup", annotation.group());
            builder.addPropertyValue("registerType", annotation.registerType());
            builder.addPropertyValue("registryAddress", annotation.registerAddress());
            builder.addPropertyValue("registryLoadBalanceType", annotation.loadBalanceType());
            builder.addPropertyValue("serializationType", annotation.serializationType());
            builder.addPropertyValue("requestTimeoutInMilliseconds", annotation.requestTimeoutInMilliseconds());
            builder.addPropertyValue("async", annotation.async());
            builder.addPropertyValue("oneway", annotation.oneway());
            builder.addPropertyValue("proxyType", annotation.proxyType());
            builder.addPropertyValue("heartbeatInterval", annotation.heartbeatInterval());
            builder.addPropertyValue("heartbeatIntervalTimeUnit", annotation.heartbeatIntervalTimeUnit());
            builder.addPropertyValue("scanNotActiveChannelInterval", annotation.scanNotActiveChannelInterval());
            builder.addPropertyValue("scanNotActiveChannelIntervalTimeUnit", annotation.scanNotActiveChannelIntervalTimeUnit());
            builder.addPropertyValue("retryIntervalInMillisecond", annotation.retryIntervalInMillisecond());
            builder.addPropertyValue("maxRetryTime", annotation.maxRetryTime());
            builder.addPropertyValue("enableResultCache", annotation.enableResultCache());
            builder.addPropertyValue("resultCacheExpire", annotation.resultCacheExpire());
            builder.addPropertyValue("enableDirectServer", annotation.enableDirectServer());
            builder.addPropertyValue("directServerUrl", annotation.directServerUrl());
            BeanDefinition beanDefinition = builder.getBeanDefinition();
            rpcConsumerBeanDefinitions.put(field.getName(), beanDefinition);
        }
    }


}
