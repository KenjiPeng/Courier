package io.kenji.courier.provider.common.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/23
 **/
@Slf4j
public class RpcProviderHandler extends SimpleChannelInboundHandler<Object> {

    private final Map<String, Object> handlerMap;

    public RpcProviderHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("RPC provider received msg ====>>> {}", msg);
        log.info("Msg info in handlerMap: ");
        handlerMap.forEach((entryKey, entryVal) -> log.info(entryKey + " === " + entryVal));
        ctx.writeAndFlush(msg);
    }
}
