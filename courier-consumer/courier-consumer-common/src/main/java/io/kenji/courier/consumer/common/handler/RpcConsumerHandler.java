package io.kenji.courier.consumer.common.handler;

import io.kenji.courier.annotation.SerializationType;
import io.kenji.courier.common.utils.GsonUtil;
import io.kenji.courier.constants.RpcConstants;
import io.kenji.courier.consumer.common.cache.ConsumerChannelCache;
import io.kenji.courier.consumer.common.context.RpcContext;
import io.kenji.courier.consumer.common.future.RpcFuture;
import io.kenji.courier.consumer.common.manager.ConsumerConnectionManager;
import io.kenji.courier.protocol.RpcProtocol;
import io.kenji.courier.protocol.enumeration.RpcStatus;
import io.kenji.courier.protocol.enumeration.RpcType;
import io.kenji.courier.protocol.header.RpcHeader;
import io.kenji.courier.protocol.header.RpcHeaderFactory;
import io.kenji.courier.protocol.request.RpcRequest;
import io.kenji.courier.protocol.response.RpcResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-13
 **/
@Slf4j
public class RpcConsumerHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {
    private volatile Channel channel;
    private SocketAddress remotePeer;

    private final Map<Long, RpcProtocol<RpcResponse>> pendingMsg = new ConcurrentHashMap<>();

    private final Map<Long, RpcFuture> pendingRpc = new ConcurrentHashMap<>();

    public Channel getChannel() {
        return channel;
    }

    public SocketAddress getRemotePeer() {
        return remotePeer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcResponse> msg) throws Exception {
        if (msg == null) return;
        log.info("Rpc consumer received data, data: {}", GsonUtil.getGson().toJson(msg));
        handleMessage(ctx.channel(), msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // if it is IdleStateEvent
        if (evt instanceof IdleStateEvent) {
            Channel channel = ctx.channel();
            log.info("IdleStateEvent is triggered, consumer tries to heart beat request to provider actively");
            RpcRequest request = new RpcRequest();
            request.setParameters(new Object[]{RpcConstants.HEART_BEAT_PING});
            RpcProtocol<RpcRequest> rpcProtocol = RpcProtocol.<RpcRequest>builder()
                    .header(RpcHeaderFactory.getRpcProtocolHeader(SerializationType.PROTOSTUFF, RpcType.HEARTBEAT_REQUEST_FROM_CONSUMER.getType()))
                    .body(request)
                    .build();
            channel.writeAndFlush(rpcProtocol);
        }
        super.userEventTriggered(ctx, evt);
    }

    private void handleMessage(Channel channel, RpcProtocol<RpcResponse> msg) {
        byte msgType = msg.getHeader().getMsgType();
        if ((byte) RpcType.HEARTBEAT_RESPONSE_FROM_PROVIDER.getType() == msgType) {
            handleHeartBeatResponseFromProvider(channel, msg);
        } else if ((byte) RpcType.HEARTBEAT_REQUEST_FROM_PROVIDER.getType() == msgType) {
            handleHeartBeatRequestFromProvider(channel, msg);
        } else if ((byte) RpcType.RESPONSE.getType() == msgType) {
            handleResponseMessage(msg);
        }
    }

    private void handleHeartBeatRequestFromProvider(Channel channel, RpcProtocol<RpcResponse> msg) {
        RpcHeader header = msg.getHeader();
        RpcResponse responseBody = msg.getBody();
        log.info("Received heart beat, requestId: {}, body: {}", header.getRequestId(), responseBody);
        RpcRequest rpcRequest = RpcRequest.builder().parameters(new Object[]{RpcConstants.HEART_BEAT_PONG}).build();
        header.setMsgType((byte) RpcType.HEARTBEAT_RESPONSE_FROM_CONSUMER.getType());
        header.setStatus((byte) RpcStatus.SUCCESS.getCode());
        RpcProtocol<RpcRequest> rpcProtocol = RpcProtocol.<RpcRequest>builder().header(header).body(rpcRequest).build();
        channel.writeAndFlush(rpcProtocol);
    }

    private void handleResponseMessage(RpcProtocol<RpcResponse> msg) {
        RpcFuture rpcFuture = pendingRpc.remove(msg.getHeader().getRequestId());
        if (rpcFuture != null) {
            rpcFuture.done(msg);
        }
    }

    //Only print heart beat response
    private void handleHeartBeatResponseFromProvider(Channel channel, RpcProtocol<RpcResponse> msg) {
        log.info("Got heart beat response from service provider {}, data: {}", channel.remoteAddress(), GsonUtil.getGson().toJson(msg));
        ConsumerConnectionManager.removeHeartBeatRecord(channel);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.remotePeer = this.channel.remoteAddress();
        ConsumerChannelCache.addChannelIntoCache(ctx.channel());
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        ConsumerChannelCache.removeChannelFromCache(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ConsumerChannelCache.removeChannelFromCache(ctx.channel());
    }

    public RpcFuture sendRequest(RpcProtocol<RpcRequest> protocol, Boolean async, boolean oneway) {
        log.info("Rpc consumer is going to send data, data: {}", GsonUtil.getGson().toJson(protocol));
        return oneway ? sendRequestOneway(protocol) : async ? sendRequestAsync(protocol) : sendRequestSync(protocol);
    }

    private RpcFuture sendRequestSync(RpcProtocol<RpcRequest> protocol) {
        log.info("Rpc consumer is going to send data synchronously, data: {}", GsonUtil.getGson().toJson(protocol));
        RpcFuture rpcFuture = this.getRpcFuture(protocol);
        channel.writeAndFlush(protocol);
        return rpcFuture;
    }

    private RpcFuture sendRequestAsync(RpcProtocol<RpcRequest> protocol) {
        log.info("Rpc consumer is going to send data asynchronously, data: {}", GsonUtil.getGson().toJson(protocol));
        RpcFuture rpcFuture = this.getRpcFuture(protocol);
        RpcContext.getContext().setRpcFuture(rpcFuture);
        channel.writeAndFlush(protocol);
        return null;
    }

    private RpcFuture sendRequestOneway(RpcProtocol<RpcRequest> protocol) {
        log.info("Rpc consumer is going to send data on one way, data: {}", GsonUtil.getGson().toJson(protocol));
        channel.writeAndFlush(protocol);
        return null;
    }

    private RpcFuture getRpcFuture(RpcProtocol<RpcRequest> protocol) {
        RpcFuture rpcFuture = new RpcFuture(protocol);
        RpcHeader header = protocol.getHeader();
        long requestId = header.getRequestId();
        pendingRpc.put(requestId, rpcFuture);
        return rpcFuture;
    }

    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        ConsumerChannelCache.removeChannelFromCache(channel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("server caught exception", cause);
        ConsumerChannelCache.removeChannelFromCache(ctx.channel());
        ctx.close();
    }
}
