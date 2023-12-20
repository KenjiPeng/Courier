package io.kenji.courier.annotation;

/**
 * @Author Kenji Peng
 * @Description The length of serializationType can not be more than 16
 * @Date 2023-12-06
 **/
public enum SerializationType {

    PROTOSTUFF,

    KRYO,

    JSON,

    JDK,

    HESSIAN2,

    FST


}
