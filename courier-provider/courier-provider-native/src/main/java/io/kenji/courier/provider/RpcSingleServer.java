package io.kenji.courier.provider;

import io.kenji.courier.annotation.Proxy;
import io.kenji.courier.common.scanner.provider.RpcProviderScanner;
import io.kenji.courier.provider.common.server.base.BaseServer;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/23
 **/
@Slf4j
public class RpcSingleServer extends BaseServer {
    public RpcSingleServer(String serverAddress, String scanPackage, Proxy proxy) {
        super(serverAddress, proxy);
        try {
            this.handlerMap = RpcProviderScanner.doScannerWithRpcProviderAnnotationFilterAndRegistryService(scanPackage);
        } catch (Exception e) {
            log.error("Encountered error whilst RPC server initialization", e);
        }
    }
}
