package io.kenji.courier.consumer.common.handler;

import io.kenji.courier.common.utils.GsonUtil;
import io.kenji.courier.consumer.common.context.RpcContext;
import io.kenji.courier.consumer.common.future.RpcFuture;
import io.kenji.courier.protocol.RpcProtocol;
import io.kenji.courier.protocol.header.RpcHeader;
import io.kenji.courier.protocol.request.RpcRequest;
import io.kenji.courier.protocol.response.RpcResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
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
        RpcFuture rpcFuture = pendingRpc.remove(msg.getHeader().getRequestId());
        if (rpcFuture != null) {
            rpcFuture.done(msg);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.remotePeer = this.channel.remoteAddress();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
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
    }
}
