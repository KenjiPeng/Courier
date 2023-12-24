package io.kenji.courier.consumer.common;

import io.kenji.courier.common.helper.RpcServiceHelper;
import io.kenji.courier.common.threadpool.ClientThreadPool;
import io.kenji.courier.common.utils.IpUtils;
import io.kenji.courier.consumer.common.consumer.Consumer;
import io.kenji.courier.consumer.common.future.RpcFuture;
import io.kenji.courier.consumer.common.handler.RpcConsumerHandler;
import io.kenji.courier.consumer.common.helper.RpcConsumerHandlerHelper;
import io.kenji.courier.consumer.common.initializer.RpcConsumerInitializer;
import io.kenji.courier.protocol.RpcProtocol;
import io.kenji.courier.protocol.meta.ServiceMeta;
import io.kenji.courier.protocol.request.RpcRequest;
import io.kenji.courier.registry.api.RegistryService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-13
 **/
@Slf4j
public class RpcConsumer implements Consumer {
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    private static volatile RpcConsumer instance;
    
    private final String localIp;

//    private static final Map<String, RpcConsumerHandler> handlerMap = new ConcurrentHashMap<>();

    private RpcConsumer() {
        this.bootstrap = new Bootstrap();
        this.eventLoopGroup = new NioEventLoopGroup(4);
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .handler(new RpcConsumerInitializer());
        localIp = IpUtils.getLocalHostIp();
    }

    public static RpcConsumer getInstance() {
        if (instance == null) {
            synchronized (RpcConsumer.class) {
                if (instance == null) {
                    instance = new RpcConsumer();
                }
            }
        }
        return instance;
    }

    public void close() {
        eventLoopGroup.shutdownGracefully();
        ClientThreadPool.shutdown();
        RpcConsumerHandlerHelper.closeConsumerHandler();
    }

    @Override
    public RpcFuture sendRequest(RpcProtocol<RpcRequest> protocol, RegistryService registryService) throws Exception {
        RpcRequest body = protocol.getBody();
        String serviceKey = RpcServiceHelper.buildServiceKey(body.getClassName(), body.getVersion(), body.getGroup());
        Object[] parameters = body.getParameters();
        int invokerHashCode = (parameters == null || parameters.length == 0) ? serviceKey.hashCode() : parameters[0].hashCode();
        Optional<ServiceMeta> serviceMetaOptional = registryService.discovery(serviceKey, invokerHashCode, localIp);
        if (serviceMetaOptional.isPresent()) {
            ServiceMeta serviceMeta = serviceMetaOptional.get();
            RpcConsumerHandler handler = RpcConsumerHandlerHelper.get(serviceMeta);
            if (handler == null) {
                handler = getRpcConsumerHandler(serviceMeta.serviceAddr(), serviceMeta.servicePort(), serviceMeta);
            } else if (!handler.getChannel().isActive()) {
                handler.close();
                handler = getRpcConsumerHandler(serviceMeta.serviceAddr(), serviceMeta.servicePort(), serviceMeta);
            }
            return handler.sendRequest(protocol, body.getAsync(), body.getOneway());
        }
        return null;
    }

    private RpcConsumerHandler getRpcConsumerHandler(String serviceAddress, int port, ServiceMeta serviceMeta) throws InterruptedException {
        ChannelFuture channelFuture = bootstrap.connect(serviceAddress, port).sync();
        channelFuture.addListener(listener -> {
            if (channelFuture.isSuccess()) {
                log.info("Connect rpc provider server {} on port {} success", serviceAddress, port);
            } else {
                log.error("Failed to connect rpc provider server {} on port {}", serviceAddress, port, channelFuture.cause());
                eventLoopGroup.shutdownGracefully();
            }
        });
        RpcConsumerHandler rpcConsumerHandler = channelFuture.channel().pipeline().get(RpcConsumerHandler.class);
        RpcConsumerHandlerHelper.put(serviceMeta, rpcConsumerHandler);
        return rpcConsumerHandler;
    }

}
