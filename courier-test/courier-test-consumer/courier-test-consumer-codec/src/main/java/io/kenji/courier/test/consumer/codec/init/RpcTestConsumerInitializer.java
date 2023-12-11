package io.kenji.courier.test.consumer.codec.init;

import io.kenji.courier.test.consumer.codec.handler.RpcTestConsumerHandler;
import io.kenji.io.kenji.courier.codec.RpcDecoder;
import io.kenji.io.kenji.courier.codec.RpcEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-11
 **/
public class RpcTestConsumerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new RpcEncoder());
        pipeline.addLast(new RpcDecoder());
        pipeline.addLast(new RpcTestConsumerHandler());
    }
}
