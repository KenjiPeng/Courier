package io.kenji.courier.provider.common.scanner;

import io.kenji.courier.annotation.RpcProvider;
import io.kenji.courier.common.helper.RpcServiceHelper;
import io.kenji.courier.common.scanner.ClassScanner;
import io.kenji.courier.protocol.meta.ServiceMeta;
import io.kenji.courier.registry.api.RegistryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/9
 **/
@Slf4j
public class RpcProviderScanner extends ClassScanner {

    public static Map<String, Object> doScannerWithRpcProviderAnnotationFilterAndRegistryService(String host, int port, String scanPackage, RegistryService registryService) throws IOException {
        Map<String, Object> handlerMap = new ConcurrentHashMap<>();
        List<String> classNameList = getClassNameList(scanPackage);
        if (classNameList == null || classNameList.size() == 0) {
            return handlerMap;
        }
        classNameList.forEach(className -> {
            try {
                Class<?> clazz = Class.forName(className);
                RpcProvider rpcProvider = clazz.getAnnotation(RpcProvider.class);
                if (rpcProvider != null) {
                    //Use interfaceClass first, and then use interfaceClassName if interfaceClass is empty
                    //TODO register provider meta info in register service
                    ServiceMeta serviceMeta = new ServiceMeta(getServiceName(rpcProvider), rpcProvider.version(), host, port, rpcProvider.group());
                    registryService.register(serviceMeta);
                    //key=serviceName+version+group, value = instance with @RpcProvider annotation
                    handlerMap.put(RpcServiceHelper.buildServiceKey(serviceMeta.serviceName(), serviceMeta.serviceVersion(), serviceMeta.serviceGroup()), clazz.getDeclaredConstructor().newInstance());
                }
            } catch (Exception e) {
                log.error("Scan classes throws exception", e);
            }
        });
        return handlerMap;
    }

    private static String getServiceName(RpcProvider rpcProvider) {
        Class<?> interfaceClass = rpcProvider.interfaceClass();
        if (interfaceClass == void.class) {
            return rpcProvider.interfaceName();
        }
        String serviceName = interfaceClass.getName();
        if (StringUtils.isBlank(serviceName)) {
            return rpcProvider.interfaceName();
        }
        return serviceName;
    }

}
