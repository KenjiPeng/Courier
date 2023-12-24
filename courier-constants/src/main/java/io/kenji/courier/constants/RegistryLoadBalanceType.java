package io.kenji.courier.constants;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-24
 **/
public enum RegistryLoadBalanceType {

    RANDOM,

    RANDOM_WEIGHT,

    ROUND_ROBIN,

    ROUND_ROBIN_WEIGHT,

    HASH,

    HASH_WEIGHT,

    SOURCE_IP_HASH
}
