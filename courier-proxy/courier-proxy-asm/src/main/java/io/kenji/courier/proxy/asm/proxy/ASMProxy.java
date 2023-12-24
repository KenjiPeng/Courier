package io.kenji.courier.proxy.asm.proxy;

import io.kenji.courier.proxy.asm.classloader.ASMClassloader;
import io.kenji.courier.proxy.asm.factory.ASMGenerateFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-23
 **/
public class ASMProxy {

    protected InvocationHandler handler;
    //proxy class name count
    private static final AtomicInteger PROXY_CNT = new AtomicInteger(0);
    private static final String PROXY_CLASS_NAME_PREFIX = "$proxy";

    public ASMProxy(InvocationHandler handler) {
        this.handler = handler;
    }

    public static Object newProxyInstance(ClassLoader classLoader, Class<?>[] interfaces, InvocationHandler invocationHandler) throws Exception {
        Class<?> proxyClass = generateProxyClass(interfaces);
        Constructor<?> constructor = proxyClass.getConstructor(InvocationHandler.class);
        return constructor.newInstance(invocationHandler);
    }


    private static Class<?> generateProxyClass(Class<?>[] interfaces) throws ClassNotFoundException {
        String proxyClassName = PROXY_CLASS_NAME_PREFIX + PROXY_CNT.getAndIncrement();
        byte[] codes = ASMGenerateFactory.generateClass(interfaces, proxyClassName);
        ASMClassloader asmClassloader = new ASMClassloader();
        asmClassloader.add(proxyClassName, codes);
        return asmClassloader.loadClass(proxyClassName);
    }
}
