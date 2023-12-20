package io.kenji.courier.constants;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/23
 **/
public class RpcConstants {
    /**
     * Message header, Fixed 32 bytes
     */
    public static final int HEADER_TOTAL_LEN = 32;
    /**
     * magic
     */
    public static final short MAGIC = 0x10;


    public static final String SERIALIZATION_TYPE_PROTOSTUFF = "PROTOSTUFF";

    public static final String SERIALIZATION_TYPE_KRYO = "KRYO";

    public static final String SERIALIZATION_TYPE_JSON = "JSON";

    public static final String SERIALIZATION_TYPE_JDK = "JDK";

    public static final String SERIALIZATION_TYPE_HESSIAN2 = "HESSIAN2";

    public static final String SERIALIZATION_TYPE_FST = "FST";
}
