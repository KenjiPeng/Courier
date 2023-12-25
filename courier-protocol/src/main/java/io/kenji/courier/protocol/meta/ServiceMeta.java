package io.kenji.courier.protocol.meta;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-17
 **/
public record ServiceMeta(String serviceName,
                          String serviceVersion,
                          String serviceAddr,
                          int servicePort,
                          String serviceGroup,

                          int weight) implements Serializable {
    @Serial
    private static final long serialVersionUID = 493746981500656632L;
}
