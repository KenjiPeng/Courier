package io.kenji.courier.provider.common.handler;

import io.kenji.courier.common.helper.RpcServiceHelper;
import io.kenji.courier.common.threadpool.ServerThreadPool;
import io.kenji.courier.protocol.RpcProtocol;
import io.kenji.courier.protocol.enumeration.RpcStatus;
import io.kenji.courier.protocol.enumeration.RpcType;
import io.kenji.courier.protocol.header.RpcHeader;
import io.kenji.courier.protocol.request.RpcRequest;
import io.kenji.courier.protocol.response.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/23
 **/
@Slf4j
public class RpcProviderHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> {

    private final Map<String, Object> handlerMap;

    public RpcProviderHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) {
        ServerThreadPool.submit(() -> {
            RpcHeader header = protocol.getHeader();
            header.setMsgType((byte) RpcType.RESPONSE.getType());
            RpcRequest requestBody = protocol.getBody();
            log.debug("Received request, requestId: {}, body: {}", header.getRequestId(), requestBody);
            RpcResponse rpcResponse = new RpcResponse();
            rpcResponse.setAsync(requestBody.isAsync());
            rpcResponse.setOneway(requestBody.isOneway());
            try {
                Object result = handle(requestBody);
                header.setStatus((byte) RpcStatus.SUCCESS.getCode());
                rpcResponse.setResult(result);
            } catch (Exception e) {
                rpcResponse.setError(e.getMessage());
                header.setStatus((byte) RpcStatus.FAIL.getCode());
                log.error("Hit exception in Rpc provider handler", e);
            }
            RpcProtocol<Object> rpcProtocol = RpcProtocol.builder()
                    .header(header)
                    .body(rpcResponse).build();
            ctx.writeAndFlush(rpcProtocol).addListener((ChannelFutureListener) future ->
                    log.debug("Send response for request, requestId: {}", header.getRequestId()));
        });
    }

    private Object handle(RpcRequest requestBody) throws Exception {
        String serviceKey = RpcServiceHelper.buildServiceKey(requestBody.getClassName(), requestBody.getVersion(), requestBody.getGroup());
        Object serviceBean = handlerMap.get(serviceKey);
        Optional.ofNullable(serviceBean).orElseThrow(() -> new IllegalArgumentException("Service is not existing, requestBody: " + requestBody));
        Class<?> serviceBeanClass = serviceBean.getClass();
        String methodName = requestBody.getMethodName();
        Class<?>[] parameterTypes = requestBody.getParameterTypes();
        Object[] parameters = requestBody.getParameters();
        for (Object parameter : parameters) {
            log.debug(parameter.toString());
        }
        for (Class<?> parameterType : parameterTypes) {
            log.debug(parameterType.getName());
        }
        return invokeMethod(serviceBean, serviceBeanClass, methodName, parameterTypes, parameters);
    }

    private Object invokeMethod(Object serviceBean, Class<?> serviceBeanClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Exception {
        Method method = serviceBeanClass.getMethod(methodName, parameterTypes);
        return method.invoke(serviceBean, parameters);
    }
}