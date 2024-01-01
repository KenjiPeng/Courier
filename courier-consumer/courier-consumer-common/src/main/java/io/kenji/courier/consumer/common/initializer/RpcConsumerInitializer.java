package io.kenji.courier.consumer.common.initializer;

import io.kenji.courier.codec.RpcDecoder;
import io.kenji.courier.codec.RpcEncoder;
import io.kenji.courier.constants.RpcConstants;
import io.kenji.courier.consumer.common.handler.RpcConsumerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-13
 **/
public class RpcConsumerInitializer extends ChannelInitializer<SocketChannel> {

    private int heartbeatInterval;
    private TimeUnit heartbeatIntervalTimeUnit;

    public RpcConsumerInitializer(int heartbeatInterval, TimeUnit heartbeatIntervalTimeUnit) {
        this.heartbeatInterval = heartbeatInterval;
        this.heartbeatIntervalTimeUnit = heartbeatIntervalTimeUnit;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(RpcConstants.ENCODER_HANDLER, new RpcEncoder());
        pipeline.addLast(RpcConstants.DECODER_HANDLER, new RpcDecoder());
        pipeline.addLast(RpcConstants.IDLE_STATE_HANDLER, new IdleStateHandler(0, 0, heartbeatInterval, heartbeatIntervalTimeUnit));
        pipeline.addLast(RpcConstants.CONSUMER_HANDLER, new RpcConsumerHandler());
    }
}
