package io.kenji.courier.spi.annotation;

import java.lang.annotation.*;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-19
 **/
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SPI {

    String value() default "";
}
