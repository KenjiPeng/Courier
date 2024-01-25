package io.kenji.courier.provider.common.server.base;

import io.kenji.courier.annotation.ReflectType;
import io.kenji.courier.annotation.RegisterType;
import io.kenji.courier.codec.RpcDecoder;
import io.kenji.courier.codec.RpcEncoder;
import io.kenji.courier.provider.common.handler.RpcProviderHandler;
import io.kenji.courier.provider.common.manager.ProviderConnectionManager;
import io.kenji.courier.provider.common.server.api.Server;
import io.kenji.courier.registry.api.RegistryService;
import io.kenji.courier.registry.api.config.RegistryConfig;
import io.kenji.courier.spi.loader.ExtensionLoader;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.kenji.courier.constants.RpcConstants.*;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/23
 **/
@Slf4j
public class BaseServer implements Server {

    protected String host = "127.0.0.1";

    protected int port = 27110;

    protected String serverAddress;

    protected String registryAddress;

    protected Map<String, Object> handlerMap = new HashMap<>();

    protected final ReflectType reflectType;

    protected RegistryService registryService;

    private ScheduledExecutorService executorService;

    // heartbeatInterval
    protected int heartbeatInterval = 30;
    protected TimeUnit heartbeatIntervalTimeUnit = TimeUnit.SECONDS;
    // scanNotActiveChannelInterval
    protected int scanNotActiveChannelInterval = 60;
    protected TimeUnit scanNotActiveChannelIntervalTimeUnit = TimeUnit.SECONDS;
    private boolean enableResultCache;
    private int resultCacheExpire = RPC_CACHE_EXPIRE_TIME;

    private int corePoolSize;
    private int maximumPoolSize;


    public BaseServer(String serverAddress, String registryAddress, RegisterType registerType, ReflectType reflectType,
                      int heartbeatInterval, TimeUnit heartbeatIntervalTimeUnit, int scanNotActiveChannelInterval, TimeUnit scanNotActiveChannelIntervalTimeUnit,
                      int corePoolSize, int maximumPoolSize,
                      int resultCacheExpire, boolean enableResultCache) {
        this.reflectType = reflectType;
        if (!StringUtils.isEmpty(serverAddress)) {
            String[] serverArray = serverAddress.split(":");
            this.host = serverArray[0];
            this.port = Integer.parseInt(serverArray[1]);
        }
        this.registryService = this.getRegistryService(registryAddress, registerType);
        if (heartbeatInterval > 0 && heartbeatIntervalTimeUnit != null) {
            this.heartbeatInterval = heartbeatInterval;
            this.heartbeatIntervalTimeUnit = heartbeatIntervalTimeUnit;
        }
        if (scanNotActiveChannelInterval > 0 && scanNotActiveChannelIntervalTimeUnit != null) {
            this.scanNotActiveChannelInterval = scanNotActiveChannelInterval;
            this.scanNotActiveChannelIntervalTimeUnit = scanNotActiveChannelIntervalTimeUnit;
        }
        if (resultCacheExpire > 0) {
            this.resultCacheExpire = resultCacheExpire;
        }
        this.enableResultCache = enableResultCache;
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
    }

    private RegistryService getRegistryService(String registryAddress, RegisterType registerType) {
        RegistryService registryService = null;
        try {
            registryService = ExtensionLoader.getExtension(RegistryService.class, registerType.name());
            registryService.init(new RegistryConfig(registryAddress, registerType, null));
        } catch (Exception e) {
            log.error("Rpc server init error", e);
        }
        return registryService;
    }

    private void startHeartBeat() {
        executorService = Executors.newScheduledThreadPool(2);
        executorService.scheduleAtFixedRate(() -> {
            log.info("========scan not active channel============");
            ProviderConnectionManager.scanNotActiveChannel();
        }, 10, scanNotActiveChannelInterval, scanNotActiveChannelIntervalTimeUnit);
        executorService.scheduleAtFixedRate(() -> {
            log.info("========broadcast ping message from provider============");
            ProviderConnectionManager.broadcastPingMessageFromProvider();
        }, 3, heartbeatInterval, heartbeatIntervalTimeUnit);
    }

    @Override
    public void startNettyServer() {
        startHeartBeat();
        NioEventLoopGroup bassGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        try {
            bootstrap.group(bassGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline()
                                    //TODO implement custom protocol
                                    .addLast(DECODER_HANDLER, new RpcDecoder())
                                    .addLast(ENCODER_HANDLER, new RpcEncoder())
                                    .addLast(IDLE_STATE_HANDLER, new IdleStateHandler(0, 0, heartbeatInterval, heartbeatIntervalTimeUnit))
                                    .addLast(PROVIDER_HANDLER, new RpcProviderHandler(handlerMap, reflectType, enableResultCache, resultCacheExpire, corePoolSize, maximumPoolSize));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future = bootstrap.bind("0.0.0.0", port).sync();
            log.info("Server started on {}:{}", host, port);
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("Encounter error while RPC server is starting ... ", e);
        } finally {
            workerGroup.shutdownGracefully();
            bassGroup.shutdownGracefully();
        }

    }
}
