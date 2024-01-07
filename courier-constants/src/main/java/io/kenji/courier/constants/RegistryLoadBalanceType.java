package io.kenji.courier.constants;

import java.util.List;

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

    SOURCE_IP_HASH,

    SOURCE_IP_HASH_WEIGHT,

    ZK_CONSISTENT_HASH,

    RANDOM_WEIGHT_ENHANCED,

    ROUND_ROBIN_WEIGHT_ENHANCED,

    HASH_WEIGHT_ENHANCED,

    SOURCE_IP_HASH_WEIGHT_ENHANCED,

    ZK_CONSISTENT_HASH_ENHANCE,

    LEAST_CONNECTION_ENHANCE;

    public boolean isEnhanced() {
        List<RegistryLoadBalanceType> enhanceLoadBalanceTypeList = List.of(
                RANDOM_WEIGHT_ENHANCED, ROUND_ROBIN_WEIGHT_ENHANCED, HASH_WEIGHT_ENHANCED,
                SOURCE_IP_HASH_WEIGHT_ENHANCED, ZK_CONSISTENT_HASH_ENHANCE,LEAST_CONNECTION_ENHANCE);
        return enhanceLoadBalanceTypeList.contains(this);
    }
}
