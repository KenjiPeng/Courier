package io.kenji.courier.proxy.api.object;

import io.kenji.courier.annotation.SerializationType;
import io.kenji.courier.protocol.RpcProtocol;
import io.kenji.courier.protocol.header.RpcHeaderFactory;
import io.kenji.courier.protocol.request.RpcRequest;
import io.kenji.courier.proxy.api.consumer.Consumer;
import io.kenji.courier.proxy.api.future.RpcFuture;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-17
 **/
@Slf4j
@AllArgsConstructor
public class ObjectProxy<T> implements InvocationHandler {

    /**
     * The Class of interface
     */
    private Class<T> clazz;

    private String serviceVersion;

    private String serviceGroup;
    /**
     * 15s by default
     */
    private long timeout = 15000;

    private Consumer consumer;

    private SerializationType serializationType;

    private boolean async;

    private boolean oneway;

    public ObjectProxy(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class == method.getDeclaringClass()) {
            String name = method.getName();
            return switch (name) {
                case "equals" -> proxy == args[0];
                case "hashCode" -> System.identityHashCode(proxy);
                case "toString" ->
                        proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(proxy));
                default -> throw new IllegalStateException(String.valueOf(method));
            };
        }
        Class<?>[] parameterTypes = method.getParameterTypes();

        RpcRequest rpcRequest = RpcRequest.builder()
                .version(this.serviceVersion)
                .className(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameterTypes(parameterTypes)
                .group(this.serviceGroup)
                .parameters(args)
                .async(async)
                .oneway(oneway)
                .build();
        RpcProtocol<RpcRequest> requestRpcProtocol = RpcProtocol.<RpcRequest>builder()
                .header(RpcHeaderFactory.getRequestHeader(serializationType))
                .body(rpcRequest)
                .build();
        log.debug("Class name in object proxy: {}", method.getDeclaringClass().getName());
        log.debug("Method name in object proxy: {}", method.getName());
        if (parameterTypes.length > 0) {
            List<String> paramTypeList = Arrays.stream(parameterTypes).map(Class::getName).toList();
            log.debug("Parameter types name in object proxy: {}", String.join(",", paramTypeList));
        }
        if (args != null && args.length > 0) {
            List<String> argList = Arrays.stream(args).map(Object::toString).toList();
            log.debug("Args in object proxy: {}", String.join(",", argList));
        }
        RpcFuture rpcFuture = this.consumer.sendRequest(requestRpcProtocol);
        return rpcFuture == null ? null : timeout > 0 ? rpcFuture.get(timeout, TimeUnit.MILLISECONDS).getResult() : rpcFuture.get().getResult();
    }
}