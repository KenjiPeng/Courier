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

    ENHANCE_RANDOM_WEIGHT;

    public boolean isEnhance() {
        List<RegistryLoadBalanceType> enhanceLoadBalanceTypeList = List.of(ENHANCE_RANDOM_WEIGHT);
        return enhanceLoadBalanceTypeList.contains(this);
    }
}
