package io.kenji.courier.provider.common.handler;

import io.kenji.courier.annotation.Proxy;
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
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

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
    private final Proxy proxy;

    public RpcProviderHandler(Map<String, Object> handlerMap, Proxy proxy) {
        this.handlerMap = handlerMap;
        this.proxy = proxy;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) {
        ServerThreadPool.submit(() -> {
            RpcHeader header = protocol.getHeader();
            header.setMsgType((byte) RpcType.RESPONSE.getType());
            RpcRequest requestBody = protocol.getBody();
            log.debug("Received request, requestId: {}, body: {}", header.getRequestId(), requestBody);
            RpcResponse rpcResponse = new RpcResponse();
            rpcResponse.setAsync(requestBody.getAsync());
            rpcResponse.setOneway(requestBody.getOneway());
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
        return switch (proxy) {
            case JDK -> invokeJDKMethod(serviceBean, serviceBeanClass, methodName, parameterTypes, parameters);
            case CGLIB -> invokeCGLIBMethod(serviceBean, serviceBeanClass, methodName, parameterTypes, parameters);
            default -> throw new UnsupportedOperationException("Only support invoke method by JDK or CGLIB");
        };
    }

    private Object invokeJDKMethod(Object serviceBean, Class<?> serviceBeanClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Exception {
        log.info("Use JDK reflect type invoke method");
        Method method = serviceBeanClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(serviceBean, parameters);
    }

    private Object invokeCGLIBMethod(Object serviceBean, Class<?> serviceBeanClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Exception {
        log.info("Use CGLIB reflect type invoke method");
        FastClass fastClass = FastClass.create(serviceBeanClass);
        FastMethod fastClassMethod = fastClass.getMethod(methodName, parameterTypes);
        return fastClassMethod.invoke(serviceBean, parameters);
    }
}
