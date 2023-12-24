package io.kenji.courier.provider.common.server.base;

import io.kenji.courier.annotation.ReflectType;
import io.kenji.courier.annotation.RegisterType;
import io.kenji.courier.codec.RpcDecoder;
import io.kenji.courier.codec.RpcEncoder;
import io.kenji.courier.provider.common.handler.RpcProviderHandler;
import io.kenji.courier.provider.common.server.api.Server;
import io.kenji.courier.registry.api.RegistryService;
import io.kenji.courier.registry.api.config.RegistryConfig;
import io.kenji.courier.registry.zookeeper.ZookeeperRegistryService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/23
 **/
@Slf4j
public class BaseServer implements Server {

    protected String host = "127.0.0.1";

    protected int port = 27110;

    protected Map<String, Object> handlerMap = new HashMap<>();

    protected final ReflectType reflectType;

    protected RegistryService registryService;


    public BaseServer(String serverAddress, String registryAddress, RegisterType registerType, ReflectType reflectType) {
        this.reflectType = reflectType;
        if (!StringUtils.isEmpty(serverAddress)) {
            String[] serverArray = serverAddress.split(":");
            this.host = serverArray[0];
            this.port = Integer.parseInt(serverArray[1]);
        }
        this.registryService = this.getRegistryService(registryAddress, registerType);
    }

    private RegistryService getRegistryService(String registryAddress, RegisterType registerType) {
        RegistryService registryService = null;
        try {
            registryService = new ZookeeperRegistryService();
            registryService.init(new RegistryConfig(registryAddress, registerType));
        } catch (Exception e) {
            log.error("Rpc server init error", e);
        }
        return registryService;
    }

    @Override
    public void startNettyServer() {
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
                                    .addLast(new RpcDecoder())
                                    .addLast(new RpcEncoder())
                                    .addLast(new RpcProviderHandler(handlerMap, reflectType));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future = bootstrap.bind(host, port).sync();
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
