package io.kenji.courier.test.consumer.codec;

import io.kenji.courier.test.consumer.codec.init.RpcTestConsumerInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-11
 **/
public class RpcTestConsumer {

    public static void main(String[] args) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
        try {
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new RpcTestConsumerInitializer());
            bootstrap.connect("127.0.0.1",27880).sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            Thread.sleep(2000);
            eventLoopGroup.shutdownGracefully();
        }
    }
}
