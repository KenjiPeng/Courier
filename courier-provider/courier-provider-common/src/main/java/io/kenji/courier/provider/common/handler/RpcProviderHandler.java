package io.kenji.courier.provider.common.handler;

import io.kenji.courier.annotation.ReflectType;
import io.kenji.courier.common.helper.RpcServiceHelper;
import io.kenji.courier.common.threadpool.ServerThreadPool;
import io.kenji.courier.common.utils.GsonUtil;
import io.kenji.courier.protocol.RpcProtocol;
import io.kenji.courier.protocol.enumeration.RpcStatus;
import io.kenji.courier.protocol.enumeration.RpcType;
import io.kenji.courier.protocol.header.RpcHeader;
import io.kenji.courier.protocol.request.RpcRequest;
import io.kenji.courier.protocol.response.RpcResponse;
import io.kenji.courier.provider.common.cache.ProviderChannelCache;
import io.kenji.courier.provider.common.manager.ProviderConnectionManager;
import io.kenji.courier.reflect.api.ReflectInvoker;
import io.kenji.courier.spi.loader.ExtensionLoader;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

import static io.kenji.courier.constants.RpcConstants.HEART_BEAT_PONG;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/23
 **/
@Slf4j
public class RpcProviderHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> {

    private final Map<String, Object> handlerMap;
    private final ReflectInvoker reflectInvoker;

    public RpcProviderHandler(Map<String, Object> handlerMap, ReflectType reflectType) {
        this.handlerMap = handlerMap;
        this.reflectInvoker = ExtensionLoader.getExtension(ReflectInvoker.class, reflectType.name());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) {
        ServerThreadPool.submit(() -> {
            RpcProtocol<RpcResponse> responseProtocol = handleMessage(protocol, ctx.channel());
            ctx.writeAndFlush(responseProtocol).addListener((ChannelFutureListener) future ->
                    log.debug("Send response for request, requestId: {}", responseProtocol.getHeader().getRequestId()));
        });
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // if it is IdleStateEvent
        if (evt instanceof IdleStateEvent) {
            Channel channel = ctx.channel();
            try {
                log.info("IdleStateEvent is triggered, provider close channel {}", channel);
                channel.close();
            } finally {
                channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        ProviderChannelCache.remove(ctx.channel());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ProviderChannelCache.add(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ProviderChannelCache.remove(ctx.channel());
    }

    private RpcProtocol<RpcResponse> handleMessage(RpcProtocol<RpcRequest> requestRpcProtocol, Channel channel) {
        byte msgType = requestRpcProtocol.getHeader().getMsgType();
        RpcProtocol<RpcResponse> responseRpcProtocol = null;
        if ((byte) RpcType.HEARTBEAT_REQUEST_FROM_CONSUMER.getType() == msgType) {
            responseRpcProtocol = handleHeartBeatRequestFromConsumer(requestRpcProtocol);
        } else if ((byte) RpcType.HEARTBEAT_RESPONSE_FROM_CONSUMER.getType() == msgType) {
            handleHeartResponseFromConsumer(requestRpcProtocol, channel);
        } else if ((byte) RpcType.REQUEST.getType() == msgType) {
            responseRpcProtocol = handleRequestMessage(requestRpcProtocol);
        }
        return responseRpcProtocol;
    }

    //Only print heart beat response
    private void handleHeartResponseFromConsumer(RpcProtocol<RpcRequest> msg, Channel channel) {
        log.info("Got heart beat from service provider {}, data: {}", channel.remoteAddress(), GsonUtil.getGson().toJson(msg));
        ProviderConnectionManager.removeHeartBeatRecord(channel);
    }

    private RpcProtocol<RpcResponse> handleRequestMessage(RpcProtocol<RpcRequest> protocol) {
        RpcHeader header = protocol.getHeader();
        header.setMsgType((byte) RpcType.RESPONSE.getType());
        RpcRequest requestBody = protocol.getBody();
        log.debug("Received request, requestId: {}, body: {}", header.getRequestId(), requestBody);
        // Create RpcResponse
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setAsync(requestBody.getAsync());
        rpcResponse.setOneway(requestBody.getOneway());
        try {
            Object result = handle(requestBody);
            header.setStatus((byte) RpcStatus.SUCCESS.getCode());
            rpcResponse.setResult(result);
        } catch (Exception e) {
            rpcResponse.setError(e.getMessage());
            header.setStatus((byte) RpcStatus.FAIL.getCode());
            log.error("Hit exception in Rpc provider handler", e);
        }
        return RpcProtocol.<RpcResponse>builder()
                .header(header)
                .body(rpcResponse).build();
    }

    private RpcProtocol<RpcResponse> handleHeartBeatRequestFromConsumer(RpcProtocol<RpcRequest> protocol) {
        RpcHeader header = protocol.getHeader();
        RpcRequest requestBody = protocol.getBody();
        log.debug("Received heart beat, requestId: {}, body: {}", header.getRequestId(), requestBody);
        // Create RpcResponse
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setAsync(requestBody.getAsync());
        rpcResponse.setOneway(requestBody.getOneway());
        header.setMsgType((byte) RpcType.HEARTBEAT_RESPONSE_FROM_PROVIDER.getType());
        header.setStatus((byte) RpcStatus.SUCCESS.getCode());
        rpcResponse.setResult(HEART_BEAT_PONG);
        return RpcProtocol.<RpcResponse>builder()
                .header(header)
                .body(rpcResponse).build();
    }

    private Object handle(RpcRequest requestBody) throws Exception {
        String serviceKey = RpcServiceHelper.buildServiceKey(requestBody.getClassName(), requestBody.getVersion(), requestBody.getGroup());
        Object serviceBean = handlerMap.get(serviceKey);
        Optional.ofNullable(serviceBean).orElseThrow(() -> new IllegalArgumentException("Service is not existing, requestBody: " + requestBody));
        Class<?> serviceBeanClass = serviceBean.getClass();
        String methodName = requestBody.getMethodName();
        Class<?>[] parameterTypes = requestBody.getParameterTypes();
        Object[] parameters = requestBody.getParameters();
        for (Object parameter : parameters) {
            log.debug(parameter.toString());
        }
        for (Class<?> parameterType : parameterTypes) {
            log.debug(parameterType.getName());
        }
        return reflectInvoker.invokeMethod(serviceBean, serviceBeanClass, methodName, parameterTypes, parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("server caught exception", cause);
        ProviderChannelCache.remove(ctx.channel());
        ctx.close();
    }
}
