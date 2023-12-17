package io.kenji.courier.test.consumer.handler;

import io.kenji.courier.annotation.SerializationType;
import io.kenji.courier.consumer.common.RpcConsumer;
import io.kenji.courier.consumer.common.callback.AsyncRpcCallback;
import io.kenji.courier.consumer.common.future.RpcFuture;
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
        RpcFuture rpcFuture = rpcConsumer.sendRequest(getRpcRequestProtocol());
        rpcFuture.addCallback(new AsyncRpcCallback() {
            @Override
            public void onSuccess(RpcResponse response) {
                log.info("Callback on success, received response: {}",response);
            }

            @Override
            public void onException(Exception e) {
                log.error("Callback on exception",e);
            }
        });
        Thread.sleep(200);
        rpcConsumer.close();
    }

    private static RpcProtocol<RpcRequest> getRpcRequestProtocol() {
        RpcRequest rpcRequest = RpcRequest.builder()
                .className("io.kenji.courier.test.api.DemoService")
                .group("Kenji")
                .methodName("hello1")
                .parameters(new Object[]{"Kenji"})
                .parameterTypes(new Class[]{String.class})
                .version("1.0.0")
                .async(false)
                .oneway(false).build();
        return RpcProtocol.<RpcRequest>builder().header(RpcHeaderFactory.getRequestHeader(SerializationType.JDK)).body(rpcRequest).build();
    }
}
