package io.kenji.courier.test.consumer.handler;

import io.kenji.courier.annotation.SerializationType;
import io.kenji.courier.consumer.common.RpcConsumer;
import io.kenji.courier.protocol.RpcProtocol;
import io.kenji.courier.protocol.header.RpcHeaderFactory;
import io.kenji.courier.protocol.request.RpcRequest;
import io.kenji.courier.protocol.response.RpcResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-14
 **/
@Slf4j
public class RpcConsumerHandlerTest {

    public static void main(String[] args) throws Exception {
        RpcConsumer rpcConsumer = RpcConsumer.getInstance();
        RpcResponse response = rpcConsumer.sendRequest(getRpcRequestProtocol());
        log.info("Received response: {}", response.getResult());
        rpcConsumer.close();
    }

    private static RpcProtocol<RpcRequest> getRpcRequestProtocol() {
        RpcRequest rpcRequest = RpcRequest.builder()
                .className("io.kenji.courier.test.api.DemoService")
                .group("Kenji")
                .methodName("hello")
                .parameters(new Object[]{"Kenji"})
                .parameterTypes(new Class[]{String.class})
                .version("1.0.0")
                .async(false)
                .oneway(false).build();
        return RpcProtocol.<RpcRequest>builder().header(RpcHeaderFactory.getRequestHeader(SerializationType.JDK)).body(rpcRequest).build();
    }
}
