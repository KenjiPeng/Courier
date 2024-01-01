package io.kenji.courier.test.consumer.handler;

import io.kenji.courier.annotation.RegisterType;
import io.kenji.courier.annotation.SerializationType;
import io.kenji.courier.consumer.common.RpcConsumer;
import io.kenji.courier.consumer.common.callback.AsyncRpcCallback;
import io.kenji.courier.consumer.common.future.RpcFuture;
import io.kenji.courier.protocol.RpcProtocol;
import io.kenji.courier.protocol.enumeration.RpcType;
import io.kenji.courier.protocol.header.RpcHeaderFactory;
import io.kenji.courier.protocol.request.RpcRequest;
import io.kenji.courier.protocol.response.RpcResponse;
import io.kenji.courier.registry.api.RegistryService;
import io.kenji.courier.registry.api.config.RegistryConfig;
import io.kenji.courier.registry.zookeeper.ZookeeperRegistryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-14
 **/
@Slf4j
public class RpcConsumerHandlerTest {

    public static void main(String[] args) throws Exception {
        RpcConsumer rpcConsumer = RpcConsumer.getInstance(10, TimeUnit.SECONDS, 10, TimeUnit.SECONDS);
        RpcFuture rpcFuture = rpcConsumer.sendRequest(getRpcRequestProtocol(), getRegistryService("127.0.0.1:2181", RegisterType.ZOOKEEPER));
        rpcFuture.addCallback(new AsyncRpcCallback() {
            @Override
            public void onSuccess(RpcResponse response) {
                log.info("Callback on success, received response: {}", response);
            }

            @Override
            public void onException(Exception e) {
                log.error("Callback on exception", e);
            }
        });
        Thread.sleep(200);
        rpcConsumer.close();
    }

    private static RpcProtocol<RpcRequest> getRpcRequestProtocol() {
        RpcRequest rpcRequest = RpcRequest.builder()
                .className("io.kenji.courier.test.api.DemoService")
                .group("Kenji")
                .methodName("hello1")
                .parameters(new Object[]{"Kenji"})
                .parameterTypes(new Class[]{String.class})
                .version("1.0.0")
                .async(false)
                .oneway(false).build();
        return RpcProtocol.<RpcRequest>builder().header(RpcHeaderFactory.getRpcProtocolHeader(SerializationType.JDK, RpcType.REQUEST.getType())).body(rpcRequest).build();
    }


    private static RegistryService getRegistryService(String registryAddress, RegisterType registerType) {
        if (StringUtils.isNotBlank(registryAddress) || registerType == null) {
            throw new IllegalArgumentException("Registry info is illegal, registryAddress = " + registryAddress + " ,registryService = " + registryAddress);
        }
        ZookeeperRegistryService registryService = new ZookeeperRegistryService();
        try {
            registryService.init(new RegistryConfig(registryAddress, registerType, null));
        } catch (Exception e) {
            log.error("Hit exception during RpClient init registry service", e);
            throw new RuntimeException(e);
        }
        return registryService;
    }
}
