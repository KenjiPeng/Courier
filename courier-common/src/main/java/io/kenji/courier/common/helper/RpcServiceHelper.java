package io.kenji.courier.common.helper;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-11
 **/
public class RpcServiceHelper {

    public static String buildServiceKey(String serviceName, String serviceVersion, String group) {
        return String.join("#", serviceName, serviceVersion, group);
    }


}
