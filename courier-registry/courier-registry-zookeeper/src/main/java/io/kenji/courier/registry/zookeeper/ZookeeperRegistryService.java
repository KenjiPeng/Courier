package io.kenji.courier.registry.zookeeper;


import io.kenji.courier.common.helper.RpcServiceHelper;
import io.kenji.courier.loadbalancer.api.ServiceLoadBalancer;
import io.kenji.courier.loadbalancer.api.helper.ServiceLoadBalancerHelper;
import io.kenji.courier.protocol.meta.ServiceMeta;
import io.kenji.courier.registry.api.RegistryService;
import io.kenji.courier.registry.api.config.RegistryConfig;
import io.kenji.courier.spi.loader.ExtensionLoader;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-17
 **/
public class ZookeeperRegistryService implements RegistryService {
    private static final int BASE_SLEEP_TIME_MS = 1000;
    private static final int MAX_RETRIES = 3;
    private static final String ZK_BASE_PATH = "/courier";

    private ServiceDiscovery<ServiceMeta> serviceDiscovery;

    private ServiceLoadBalancer<ServiceInstance<ServiceMeta>> serviceLoadBalancer;

    private ServiceLoadBalancer<ServiceMeta> serviceEnhanceLoadBalancer;

    @Override
    public void register(ServiceMeta serviceMeta) throws Exception {
        ServiceInstance<ServiceMeta> serviceInstance = createServiceInstance(serviceMeta);
        this.serviceDiscovery.registerService(serviceInstance);
    }

    @Override
    public void unregister(ServiceMeta serviceMeta) throws Exception {
        ServiceInstance<ServiceMeta> serviceInstance = createServiceInstance(serviceMeta);
        serviceDiscovery.unregisterService(serviceInstance);
    }

    @Override
    public Optional<ServiceMeta> discovery(String serviceName, int invokerHashCode, String sourceIp) throws Exception {
        List<ServiceInstance<ServiceMeta>> serviceInstances = serviceDiscovery.queryForInstances(serviceName).stream().toList();
        if (this.serviceLoadBalancer!=null){
            return Optional.ofNullable(this.serviceLoadBalancer.select(serviceInstances, invokerHashCode, sourceIp)).map(ServiceInstance::getPayload);
        }
        return Optional.ofNullable(this.serviceEnhanceLoadBalancer.select(ServiceLoadBalancerHelper.getServiceMetaList(serviceInstances),invokerHashCode,sourceIp));
    }


    @Override
    public void destroy() throws IOException {
        serviceDiscovery.close();
    }

    @Override
    public void init(RegistryConfig registryConfig) throws Exception {
        if (registryConfig.registryLoadBalanceType() != null) {
            ServiceLoadBalancer loadBalancer = ExtensionLoader.getExtension(ServiceLoadBalancer.class, registryConfig.registryLoadBalanceType().name());
            if (registryConfig.registryLoadBalanceType().isEnhance()){
                this.serviceEnhanceLoadBalancer = loadBalancer;
            }else {
                this.serviceLoadBalancer = loadBalancer;
            }
        }
        CuratorFramework client = CuratorFrameworkFactory.newClient(registryConfig.registryAddr(), new ExponentialBackoffRetry(BASE_SLEEP_TIME_MS, MAX_RETRIES));
        client.start();
        JsonInstanceSerializer<ServiceMeta> serializer = new JsonInstanceSerializer<>(ServiceMeta.class);
        this.serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceMeta.class)
                .client(client)
                .serializer(serializer)
                .basePath(ZK_BASE_PATH)
                .build();
        this.serviceDiscovery.start();
    }

    private ServiceInstance<ServiceMeta> createServiceInstance(ServiceMeta serviceMeta) throws Exception {
        return ServiceInstance.<ServiceMeta>builder()
                .name(RpcServiceHelper.buildServiceKey(serviceMeta.serviceName(), serviceMeta.serviceVersion(), serviceMeta.serviceGroup()))
                .address(serviceMeta.serviceAddr())
                .port(serviceMeta.servicePort())
                .payload(serviceMeta)
                .build();
    }
}
