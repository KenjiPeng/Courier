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


    public static final String HEART_BEAT_PONG = "pong";

    public static final String HEART_BEAT_PING = "ping";

    public static final String DECODER_HANDLER = "Decoder";

    public static final String ENCODER_HANDLER = "Encoder";

    public static final String IDLE_STATE_HANDLER = "IdleStateHandler";

    public static final String PROVIDER_HANDLER = "RpcProviderHandler";

    public static final String CONSUMER_HANDLER = "RpcConsumerHandler";

    public static final int RPC_SCAN_CACHE_TIME_INTERVAL = 1000; // MilliSecond

    public static final int RPC_CACHE_EXPIRE_TIME = 5000; // MilliSecond

    public static final String IP_PORT_SPLIT = ":";

    public static final String RPC_MULTI_DIRECT_SERVER_URL_SEPARATOR = ",";
}
