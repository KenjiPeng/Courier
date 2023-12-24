package io.kenji.courier.proxy.asm.classloader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-23
 **/
public class ASMClassloader extends ClassLoader {

    private final Map<String, byte[]> classMap = new ConcurrentHashMap<>();

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (classMap.containsKey(name)) {
            byte[] bytes = classMap.remove(name);
            return defineClass(name, bytes, 0, bytes.length);
        }
        return super.findClass(name);
    }

    public void add(String name, byte[] bytes) {
        classMap.put(name, bytes);
    }
}
