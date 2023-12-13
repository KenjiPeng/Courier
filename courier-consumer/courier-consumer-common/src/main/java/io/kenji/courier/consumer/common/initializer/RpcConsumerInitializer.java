package io.kenji.courier.consumer.common.initializer;

import io.kenji.courier.codec.RpcDecoder;
import io.kenji.courier.codec.RpcEncoder;
import io.kenji.courier.consumer.common.handler.RpcConsumerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-13
 **/
public class RpcConsumerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new RpcEncoder());
        pipeline.addLast(new RpcDecoder());
        pipeline.addLast(new RpcConsumerHandler());
    }
}
