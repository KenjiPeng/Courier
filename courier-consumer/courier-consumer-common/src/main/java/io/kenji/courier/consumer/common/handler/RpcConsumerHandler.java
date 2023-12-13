package io.kenji.courier.consumer.common.handler;

import io.kenji.courier.common.utils.GsonUtil;
import io.kenji.courier.protocol.RpcProtocol;
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

    private Map<Long, RpcProtocol<RpcResponse>> pendingMsg = new ConcurrentHashMap<>();

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
        pendingMsg.put(msg.getHeader().getRequestId(), msg);
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

    public RpcResponse sendRequest(RpcProtocol<RpcRequest> protocol) {
        log.info("Rpc consumer is going to send data, data: {}", GsonUtil.getGson().toJson(protocol));
        channel.writeAndFlush(protocol);
        while (true){
            long requestId = protocol.getHeader().getRequestId();
            if (pendingMsg.get(requestId)!=null){
                return pendingMsg.remove(requestId).getBody();
            }
        }
    }

    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }
}
