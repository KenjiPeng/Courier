package io.kenji.courier.provider.common.handler;

import io.kenji.courier.annotation.ReflectType;
import io.kenji.courier.common.helper.RpcServiceHelper;
import io.kenji.courier.common.threadpool.ServerThreadPool;
import io.kenji.courier.protocol.RpcProtocol;
import io.kenji.courier.protocol.enumeration.RpcStatus;
import io.kenji.courier.protocol.enumeration.RpcType;
import io.kenji.courier.protocol.header.RpcHeader;
import io.kenji.courier.protocol.request.RpcRequest;
import io.kenji.courier.protocol.response.RpcResponse;
import io.kenji.courier.reflect.api.ReflectInvoker;
import io.kenji.courier.spi.loader.ExtensionLoader;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

import static io.kenji.courier.constants.RpcConstants.HEART_BEAT_PONG;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/23
 **/
@Slf4j
public class RpcProviderHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> {

    private final Map<String, Object> handlerMap;
    private final ReflectInvoker reflectInvoker;

    public RpcProviderHandler(Map<String, Object> handlerMap, ReflectType reflectType) {
        this.handlerMap = handlerMap;
        this.reflectInvoker = ExtensionLoader.getExtension(ReflectInvoker.class, reflectType.name());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) {
        ServerThreadPool.submit(() -> {
            RpcProtocol<RpcResponse> responseProtocol = handleMessage(protocol);
            ctx.writeAndFlush(responseProtocol).addListener((ChannelFutureListener) future ->
                    log.debug("Send response for request, requestId: {}", responseProtocol.getHeader().getRequestId()));
        });
    }

    private RpcProtocol<RpcResponse> handleMessage(RpcProtocol<RpcRequest> requestRpcProtocol) {
        byte msgType = requestRpcProtocol.getHeader().getMsgType();
        RpcProtocol<RpcResponse> responseRpcProtocol = null;
        if ((byte) RpcType.HEARTBEAT.getType() == msgType) {
            responseRpcProtocol = handleHeartBeatMessage(requestRpcProtocol);
        } else if ((byte) RpcType.REQUEST.getType() == msgType) {
            responseRpcProtocol = handleRequestMessage(requestRpcProtocol);
        }
        return responseRpcProtocol;
    }

    private RpcProtocol<RpcResponse> handleRequestMessage(RpcProtocol<RpcRequest> protocol) {
        RpcHeader header = protocol.getHeader();
        header.setMsgType((byte) RpcType.RESPONSE.getType());
        RpcRequest requestBody = protocol.getBody();
        log.debug("Received request, requestId: {}, body: {}", header.getRequestId(), requestBody);
        // Create RpcResponse
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
        return RpcProtocol.<RpcResponse>builder()
                .header(header)
                .body(rpcResponse).build();
    }

    private RpcProtocol<RpcResponse> handleHeartBeatMessage(RpcProtocol<RpcRequest> protocol) {
        RpcHeader header = protocol.getHeader();
        RpcRequest requestBody = protocol.getBody();
        log.debug("Received heart beat, requestId: {}, body: {}", header.getRequestId(), requestBody);
        // Create RpcResponse
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setAsync(requestBody.getAsync());
        rpcResponse.setOneway(requestBody.getOneway());
        header.setStatus((byte) RpcStatus.SUCCESS.getCode());
        rpcResponse.setResult(HEART_BEAT_PONG);
        return RpcProtocol.<RpcResponse>builder()
                .header(header)
                .body(rpcResponse).build();
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
//        return invokeMethod(serviceBean, serviceBeanClass, methodName, parameterTypes, parameters);
        return reflectInvoker.invokeMethod(serviceBean, serviceBeanClass, methodName, parameterTypes, parameters);
    }

//    private Object invokeMethod(Object serviceBean, Class<?> serviceBeanClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Exception {
//        return switch (proxy) {
//            case JDK -> invokeJDKMethod(serviceBean, serviceBeanClass, methodName, parameterTypes, parameters);
//            case CGLIB -> invokeCGLIBMethod(serviceBean, serviceBeanClass, methodName, parameterTypes, parameters);
//            default -> throw new UnsupportedOperationException("Only support invoke method by JDK or CGLIB");
//        };
//    }
//
//    private Object invokeJDKMethod(Object serviceBean, Class<?> serviceBeanClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Exception {
//        log.info("Use JDK reflect type invoke method");
//        Method method = serviceBeanClass.getMethod(methodName, parameterTypes);
//        method.setAccessible(true);
//        return method.invoke(serviceBean, parameters);
//    }
//
//    private Object invokeCGLIBMethod(Object serviceBean, Class<?> serviceBeanClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) throws Exception {
//        log.info("Use CGLIB reflect type invoke method");
//        FastClass fastClass = FastClass.create(serviceBeanClass);
//        FastMethod fastClassMethod = fastClass.getMethod(methodName, parameterTypes);
//        return fastClassMethod.invoke(serviceBean, parameters);
//    }
}
