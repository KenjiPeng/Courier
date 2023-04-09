package io.kenji.courier.common.scanner.provider;

import io.kenji.courier.annotation.RpcProvider;
import io.kenji.courier.common.scanner.ClassScanner;
import lombok.extern.slf4j.Slf4j;

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

    public static Map<String, Object> doScannerWithRpcProviderAnnotationFilterAndRegistryService(/*String host,int port,*/String scanPackage/*,RegistryService registryService*/) throws IOException {
        Map<String, Object> handlerMap = new ConcurrentHashMap<>();
        List<String> classNameList = getClassNameList(scanPackage);
        if (classNameList == null || classNameList.isEmpty()) {
            return handlerMap;
        }

        classNameList.stream().forEach(className ->
        {
            try {
                Class<?> clazz = Class.forName(className);
                RpcProvider rpcProvider = clazz.getAnnotation(RpcProvider.class);
                if (rpcProvider != null) {
                    log.info("The class instance name annotated with the @RpcProvider annotation ===> " + clazz.getName());
                    log.info("The flied info in @RpcProvider annotation: ");
                    log.info("interfaceClass ===> " + rpcProvider.interfaceClass().getName());
                    log.info("interfaceName ===> " + rpcProvider.interfaceName());
                    log.info("version ===> " + rpcProvider.version());
                    log.info("group ===> " + rpcProvider.group());
                }
            } catch (ClassNotFoundException e) {
                log.error("Scan classes throws exception: {}", e);
            }
        });
        return handlerMap;
    }

}
