package io.kenji.courier.test.consumer.codec.handler;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.kenji.courier.annotation.SerializationType;
import io.kenji.courier.common.utils.GsonUtil;
import io.kenji.courier.protocol.RpcProtocol;
import io.kenji.courier.protocol.header.RpcHeaderFactory;
import io.kenji.courier.protocol.request.RpcRequest;
import io.kenji.courier.protocol.response.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-11
 **/
@Slf4j
public class RpcTestConsumerHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcResponse> msg) throws Exception {
        log.info("Rpc consumer received data = {}", new Gson().toJson(msg));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Start to send data...");
        RpcRequest rpcRequest = RpcRequest.builder()
                .className("ic.kenji.courier.test.DemoService")
                .group("kenji")
                .methodName("hello")
                .parameters(new Object[]{"kenji"})
                .parameterTypes(new Class[]{String.class})
                .version("1.0.0")
                .async(false)
                .oneway(false).build();
        RpcProtocol<Object> rpcProtocol = RpcProtocol.builder().header(RpcHeaderFactory.getRequestHeader(SerializationType.JDK)).body(rpcRequest).build();
        log.info("Rpc consumer send data = {}", GsonUtil.getGson().toJson(rpcProtocol));
        ctx.writeAndFlush(rpcProtocol);
        log.info("Finish sending data...");
    }


}