package io.kenji.courier.common.scanner.consumer;

import io.kenji.courier.annotation.RpcConsumer;
import io.kenji.courier.common.scanner.ClassScanner;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/9
 **/
@Slf4j
public class RpcConsumerScanner extends ClassScanner {


    public static Map<String, Object> doScannerWithRpcConsumerAnnotationFilter(/*String host,int port,*/String scanPackage/*,RegistryService registryService*/) throws IOException {
        Map<String, Object> handlerMap = new ConcurrentHashMap<>();
        List<String> classNameList = getClassNameList(scanPackage);
        if (classNameList == null || classNameList.size() == 0) {
            return handlerMap;
        }
        classNameList.forEach(className -> {
            try {
                Class<?> clazz = Class.forName(className);
                Field[] declaredFields = clazz.getDeclaredFields();
                Arrays.stream(declaredFields).forEach(field -> {
                    RpcConsumer rpcConsumer = field.getAnnotation(RpcConsumer.class);
                    if (rpcConsumer != null) {
                        log.info("The filed name annotated with the @RpcConsumer annotation ===> " + field.getName());
                        log.info("The flied info in @RpcConsumer annotation: ");
                        log.info("version ===> " + rpcConsumer.version());
                        log.info("group ===> " + rpcConsumer.group());
                        log.info("registerType ===> " + rpcConsumer.registerType());
                        log.info("registerAddress ===> " + rpcConsumer.registerAddress());

                    }
                });
            } catch (ClassNotFoundException e) {
                log.error("Scan classes throws exception.", e);
            }
        });
        return handlerMap;
    }
}
