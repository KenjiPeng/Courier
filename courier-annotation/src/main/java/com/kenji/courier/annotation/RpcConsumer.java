package com.kenji.courier.annotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author Kenji Peng
 * @Description Annotation for RPC consumer
 * @Date 2023/3/4
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Autowired
public @interface RpcConsumer {

    /**
     * Service version
     */
    String version() default "1.0.0";

    /**
     * Type of register including zookeeper, nacos, etcd, consul
     */
    String registerType() default "zookeeper";

    /**
     * Register Address
     */
    String registerAddress() default "127.0.0.1:2181";

    /**
     *Type of load balance, zk consistent hash by default
     */
    String loadBalanceType() default "zkconsistenthash";

    /**
     * Type of serialization including protostuff, kryo, json, jdk, hessian2, fst
     */
    String serializationType() default "protostuff";

    /**
     * Timeout, 5s by default
     */
    long timeout() default 5000;

    /**
     * Asynchronous execution
     */
    boolean async() default false;

    /**
     * Oneway execution
     */
    boolean oneway() default false;

    /**
     * Type of proxy including jdk, javassist, cglib
     * jdk: jdk proxy
     * javassist: javassist proxy
     * cglib: cglib proxy
     */
    String proxy() default "jdk";

    /**
     * Service group
     */
    String group() default "";
}
