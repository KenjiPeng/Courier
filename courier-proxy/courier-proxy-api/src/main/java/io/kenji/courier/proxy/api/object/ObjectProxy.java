package io.kenji.courier.proxy.api.object;

import io.kenji.courier.annotation.SerializationType;
import io.kenji.courier.cache.result.CacheResultKey;
import io.kenji.courier.cache.result.CacheResultManager;
import io.kenji.courier.constants.RpcConstants;
import io.kenji.courier.consumer.common.consumer.Consumer;
import io.kenji.courier.consumer.common.context.RpcContext;
import io.kenji.courier.consumer.common.future.RpcFuture;
import io.kenji.courier.protocol.RpcProtocol;
import io.kenji.courier.protocol.enumeration.RpcType;
import io.kenji.courier.protocol.header.RpcHeaderFactory;
import io.kenji.courier.protocol.request.RpcRequest;
import io.kenji.courier.proxy.api.async.IAsyncObjectProxy;
import io.kenji.courier.registry.api.RegistryService;
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
public class ObjectProxy<T> implements InvocationHandler, IAsyncObjectProxy {

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

    private Boolean async;

    private Boolean oneway;

    private RegistryService registryService;

    private boolean enableResultCache;

    private CacheResultManager<Object> cacheResultManager;

    public ObjectProxy(Class<T> clazz, String serviceVersion, String serviceGroup, long timeout, Consumer consumer, SerializationType serializationType,
                       Boolean async, Boolean oneway, RegistryService registryService, boolean enableResultCache, int resultCacheExpire) {
        this.clazz = clazz;
        this.serviceVersion = serviceVersion;
        this.serviceGroup = serviceGroup;
        this.timeout = timeout;
        this.consumer = consumer;
        this.serializationType = serializationType;
        this.async = async;
        this.oneway = oneway;
        this.registryService = registryService;
        this.enableResultCache = enableResultCache;
        if (resultCacheExpire <= 0) {
            resultCacheExpire = RpcConstants.RPC_CACHE_EXPIRE_TIME;
        }
        this.cacheResultManager = CacheResultManager.getInstance(resultCacheExpire, enableResultCache);
    }

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
        if (enableResultCache) return invokeSendRequestMethodCache(method, args);
        return invokeSendRequestMethod(method, args);
    }

    private Object invokeSendRequestMethod(Method method, Object[] args) throws Exception {
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
                .header(RpcHeaderFactory.getRpcProtocolHeader(serializationType, RpcType.REQUEST.getType()))
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
        RpcFuture rpcFuture = this.consumer.sendRequest(requestRpcProtocol, registryService);
        return rpcFuture == null ? null : timeout > 0 ? rpcFuture.get(timeout, TimeUnit.MILLISECONDS).getResult() : rpcFuture.get().getResult();
    }

    private Object invokeSendRequestMethodCache(Method method, Object[] args) throws Exception {
        CacheResultKey cacheResultKey = new CacheResultKey(method.getDeclaringClass().getName(), method.getName(), method.getParameterTypes(), args, this.serviceVersion, this.serviceGroup);
        Object result;
        result = cacheResultManager.get(cacheResultKey);
        if (result == null) {
            result = this.invokeSendRequestMethod(method, args);
            cacheResultKey.setCacheTimeStamp(System.currentTimeMillis());
            if (result != null) cacheResultManager.put(cacheResultKey, result);
        }
        return result;
    }

    @Override
    public RpcFuture call(String funcName, Object... args) {
        RpcProtocol<RpcRequest> request = createRequest(this.clazz.getName(), funcName, args);
        RpcFuture rpcFuture = null;
        try {
            this.consumer.sendRequest(request, registryService);
            rpcFuture = RpcContext.getContext().getRpcFuture();
        } catch (Exception e) {
            log.error("Hit error during rpc consumer sends request asynchronously", e);
        }
        return rpcFuture;
    }

    private RpcProtocol<RpcRequest> createRequest(String className, String funcName, Object... args) {
        var parameterTypes = Arrays.stream(args).map(Object::getClass).toArray(Class[]::new);
        RpcRequest rpcRequest = RpcRequest.builder()
                .version(this.serviceVersion)
                .className(className)
                .methodName(funcName)
                .parameterTypes(parameterTypes)
                .group(this.serviceGroup)
                .parameters(args)
                .async(async)
                .oneway(oneway)
                .build();
        log.debug("Class name in object proxy: {}", className);
        log.debug("Method name in object proxy: {}", funcName);
        if (parameterTypes.length > 0) {
            List<String> paramTypeList = Arrays.stream(parameterTypes).map(Class::getName).toList();
            log.debug("Parameter types name in object proxy: {}", String.join(",", paramTypeList));
        }
        if (args.length > 0) {
            List<String> argList = Arrays.stream(args).map(Object::toString).toList();
            log.debug("Args in object proxy: {}", String.join(",", argList));
        }
        return RpcProtocol.<RpcRequest>builder()
                .header(RpcHeaderFactory.getRpcProtocolHeader(serializationType, RpcType.REQUEST.getType()))
                .body(rpcRequest)
                .build();
    }
}
