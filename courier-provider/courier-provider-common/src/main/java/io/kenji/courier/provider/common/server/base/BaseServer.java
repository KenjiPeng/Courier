package io.kenji.courier.provider.common.server.base;

import io.kenji.courier.provider.common.handler.RpcProviderHandler;
import io.kenji.courier.provider.common.server.api.Server;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
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

    public BaseServer(String serverAddress) {
        if (!StringUtils.isEmpty(serverAddress)) {
            String[] serverArray = serverAddress.split(":");
            this.host = serverArray[0];
            this.port = Integer.parseInt(serverArray[1]);
        }
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
                                    .addLast(new StringDecoder())
                                    .addLast(new StringEncoder())
                                    .addLast(new RpcProviderHandler(handlerMap));
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
