package io.kenji.courier.provider;

import io.kenji.courier.annotation.Proxy;
import io.kenji.courier.annotation.RegisterType;
import io.kenji.courier.provider.common.scanner.RpcProviderScanner;
import io.kenji.courier.provider.common.server.base.BaseServer;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/23
 **/
@Slf4j
public class RpcSingleServer extends BaseServer {
    public RpcSingleServer(String serverAddress, String registryAddress, RegisterType registerType, String scanPackage, Proxy proxy) {
        super(serverAddress, registryAddress, registerType, proxy);
        try {
            this.handlerMap = RpcProviderScanner.doScannerWithRpcProviderAnnotationFilterAndRegistryService(this.host, this.port, scanPackage, registryService);
        } catch (Exception e) {
            log.error("Encountered error whilst RPC server initialization", e);
        }
    }
}
