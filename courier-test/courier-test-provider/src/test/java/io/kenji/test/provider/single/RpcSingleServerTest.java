package io.kenji.test.provider.single;

import io.kenji.courier.provider.RpcSingleServer;
import org.junit.Test;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/23
 **/
public class RpcSingleServerTest {
    @Test
    public void startRpcSingleServer(){
        RpcSingleServer singleServer = new RpcSingleServer("127.0.0.1:27880", "io.kenji.test");
        singleServer.startNettyServer();
    }
}
