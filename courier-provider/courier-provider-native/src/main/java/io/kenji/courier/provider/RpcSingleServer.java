package io.kenji.courier.provider;

import io.kenji.courier.annotation.ReflectType;
import io.kenji.courier.annotation.RegisterType;
import io.kenji.courier.provider.common.scanner.RpcProviderScanner;
import io.kenji.courier.provider.common.server.base.BaseServer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/23
 **/
@Slf4j
public class RpcSingleServer extends BaseServer {
    private RpcSingleServer(String serverAddress, String registryAddress, RegisterType registerType, ReflectType reflectType, String scanPackage, int heartbeatInterval, TimeUnit heartbeatIntervalTimeUnit, int scanNotActiveChannelInterval, TimeUnit scanNotActiveChannelIntervalTimeUnit) {
        super(serverAddress, registryAddress, registerType, reflectType, heartbeatInterval, heartbeatIntervalTimeUnit, scanNotActiveChannelInterval, scanNotActiveChannelIntervalTimeUnit);
        try {
            this.handlerMap = RpcProviderScanner.doScannerWithRpcProviderAnnotationFilterAndRegistryService(this.host, this.port, scanPackage, registryService);
        } catch (Exception e) {
            log.error("Encountered error whilst RPC server initialization", e);
        }
    }

    public static RpcSingleServerBuilder builder() {
        return new RpcSingleServerBuilder();
    }

    public static class RpcSingleServerBuilder {
        private String serverAddress;

        private String registryAddress;

        private RegisterType registerType;

        private ReflectType reflectType;
        private String scanPackage;
        private int heartbeatInterval;
        private TimeUnit heartbeatIntervalTimeUnit;
        private int scanNotActiveChannelInterval;
        private TimeUnit scanNotActiveChannelIntervalTimeUnit;


        public RpcSingleServerBuilder() {
        }

        public RpcSingleServerBuilder serverAddress(String serverAddress) {
            this.serverAddress = serverAddress;
            return this;
        }

        public RpcSingleServerBuilder registryAddress(String registryAddress) {
            this.registryAddress = registryAddress;
            return this;
        }

        public RpcSingleServerBuilder registerType(RegisterType registerType) {
            this.registerType = registerType;
            return this;
        }

        public RpcSingleServerBuilder reflectType(ReflectType reflectType) {
            this.reflectType = reflectType;
            return this;
        }

        public RpcSingleServerBuilder scanPackage(String scanPackage) {
            this.scanPackage = scanPackage;
            return this;
        }

        public RpcSingleServerBuilder heartbeatInterval(int heartbeatInterval) {
            this.heartbeatInterval = heartbeatInterval;
            return this;
        }

        public RpcSingleServerBuilder heartbeatIntervalTimeUnit(TimeUnit heartbeatIntervalTimeUnit) {
            this.heartbeatIntervalTimeUnit = heartbeatIntervalTimeUnit;
            return this;
        }

        public RpcSingleServerBuilder scanNotActiveChannelInterval(int scanNotActiveChannelInterval) {
            this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
            return this;
        }

        public RpcSingleServerBuilder scanNotActiveChannelIntervalTimeUnit(TimeUnit scanNotActiveChannelIntervalTimeUnit) {
            this.scanNotActiveChannelIntervalTimeUnit = scanNotActiveChannelIntervalTimeUnit;
            return this;
        }

        public RpcSingleServer build() {
            return new RpcSingleServer(this.serverAddress,this.registryAddress,this.registerType,this.reflectType,this.scanPackage,this.heartbeatInterval,this.heartbeatIntervalTimeUnit,
                    this.scanNotActiveChannelInterval,this.scanNotActiveChannelIntervalTimeUnit);
        }

    }
}
