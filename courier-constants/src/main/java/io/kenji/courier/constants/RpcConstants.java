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

    public static final String SERIALIZATION_TYPE_JDK = "JDK";

    public static final String SERVICE_LOAD_BALANCER_RANDOM = "random";

    public static final int SERVICE_PROVIDER_WEIGHT_MAX = 100;

    public static final int SERVICE_PROVIDER_WEIGHT_MIN = 0;
}
