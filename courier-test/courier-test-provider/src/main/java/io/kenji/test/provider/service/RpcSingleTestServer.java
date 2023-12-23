package io.kenji.test.provider.service;

import io.kenji.courier.annotation.Proxy;
import io.kenji.courier.annotation.RegisterType;
import io.kenji.courier.provider.RpcSingleServer;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/23
 **/
public class RpcSingleTestServer {

    public static void main(String[] args) {
        RpcSingleServer singleServer = new RpcSingleServer("127.0.0.1:27880", "127.0.0.1:2181", RegisterType.ZOOKEEPER, "io.kenji.test", Proxy.JDK);
        singleServer.startNettyServer();
    }

}
