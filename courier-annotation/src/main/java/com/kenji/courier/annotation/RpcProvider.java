package com.kenji.courier.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author Kenji Peng
 * @Description Annotation of RPC provider
 * @Date 2023/3/4
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RpcProvider {

    /**
     * Class of interface
     */
    Class<?> interfaceClass() default void.class;

    /**
     * ClassName of interface
     */
    String interfaceName() default "";

    /**
     * version
     */
    String version() default "1.0.0";

    /**
     * service group
     * @return
     */
    String group() default "";


}
