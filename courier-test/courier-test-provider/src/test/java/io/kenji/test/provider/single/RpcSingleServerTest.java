package io.kenji.test.provider.single;

import io.kenji.courier.annotation.ReflectType;
import io.kenji.courier.annotation.RegisterType;
import io.kenji.courier.provider.RpcSingleServer;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/23
 **/
public class RpcSingleServerTest {

    @Test
    public void startRpcSingleServer() {
        RpcSingleServer rpcSingleServer = RpcSingleServer.builder()
                .serverAddress("127.0.0.1:27880")
                .registryAddress("127.0.0.1:2181")
                .registerType(RegisterType.ZOOKEEPER)
                .scanPackage("io.kenji.test")
                .reflectType(ReflectType.ASM)
                .heartbeatInterval(10)
                .heartbeatIntervalTimeUnit(TimeUnit.SECONDS)
                .scanNotActiveChannelInterval(10)
                .scanNotActiveChannelIntervalTimeUnit(TimeUnit.SECONDS)
                .build();
        rpcSingleServer.startNettyServer();
    }
}
