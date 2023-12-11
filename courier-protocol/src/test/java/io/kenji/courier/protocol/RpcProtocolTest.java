package io.kenji.courier.protocol;

import io.kenji.courier.annotation.SerializationType;
import io.kenji.courier.protocol.header.RpcHeader;
import io.kenji.courier.protocol.header.RpcHeaderFactory;
import io.kenji.courier.protocol.request.RpcRequest;
import org.junit.Test;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/23
 **/
public class RpcProtocolTest {
    @Test
    public void testProtocol(){
        RpcHeader header = RpcHeaderFactory.getRequestHeader(SerializationType.JDK);
        RpcRequest body = RpcRequest.builder()
                .oneway(false)
                .async(false)
                .className("io.kenji.courier.demo.RpcProtocol")
                .methodName("hello")
                .group("kenji")
                .parameters(new Object[]{"kenji"})
                .parameterTypes(new Class[]{String.class})
                .version("1.0.0")
                .build();
        RpcProtocol<RpcRequest> protocol = RpcProtocol.<RpcRequest>builder().header(header).body(body).build();
    }
}
