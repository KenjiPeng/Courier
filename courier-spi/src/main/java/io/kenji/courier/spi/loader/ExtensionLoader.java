package io.kenji.courier.spi.loader;

import io.kenji.courier.spi.annotation.SPI;
import io.kenji.courier.spi.annotation.SPIClass;
import io.kenji.courier.spi.factory.ExtensionFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-19
 **/
@Slf4j
public class ExtensionLoader<T> {

    private static final Map<Class<?>, ExtensionLoader<?>> LOADERS = new ConcurrentHashMap<>();
    private final Class<T> clazz;
    private final ClassLoader classLoader;

    private String cachedDefaultName;

    private final Map<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();
    private static final String SERVICES_DIRECTORY = "META-INF/services/";
    private static final String KENJI_DIRECTORY = "META-INF/kenji/";
    private static final String KENJI_DIRECTORY_EXTERNAL = "META-INF/kenji/external/";
    private static final String KENJI_DIRECTORY_INTERNAL = "META-INF/kenji/internal/";

    private static final String[] SPI_DIRECTORIES = new String[]{
            SERVICES_DIRECTORY,
            KENJI_DIRECTORY,
            KENJI_DIRECTORY_EXTERNAL,
            KENJI_DIRECTORY_INTERNAL
    };
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();
    private final Map<Class<?>, Object> spiClassInstances = new ConcurrentHashMap<>();

    public ExtensionLoader(final Class<T> clazz, final ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.clazz = clazz;
        if (!Objects.equals(clazz, ExtensionFactory.class)) {
            ExtensionLoader.getExtensionLoader(ExtensionFactory.class).getExtensionClasses();
        }
    }

    private Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classes = cachedClasses.getValue();
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.getValue();
                if (classes == null) {
                    classes = loadExtensionClass();
                    cachedClasses.setValue(classes);
                }
            }
        }
        return classes;
    }

    private Map<String, Class<?>> loadExtensionClass() {
        SPI annotation = clazz.getAnnotation(SPI.class);
        if (annotation != null) {
            String value = annotation.value();
            if (StringUtils.isNotBlank(value)) {
                cachedDefaultName = value;
            }
        }
        Map<String, Class<?>> classes = new HashMap<>(16);
        loadDirectory(classes);
        return classes;
    }

    private void loadDirectory(final Map<String, Class<?>> classes) {
        for (String directory : SPI_DIRECTORIES) {
            String fileName = directory + clazz.getName();
            try {
                Enumeration<URL> urls = this.classLoader != null ? classLoader.getResources(fileName) : ClassLoader.getSystemResources(fileName);
                if (urls != null) {
                    while (urls.hasMoreElements()) {
                        URL url = urls.nextElement();
                        loadResources(classes, url);
                    }
                }
            } catch (IOException e) {
                log.error("Hit error during loading extension resources, fileName = {}", fileName, e);
            }
        }
    }

    private void loadResources(final Map<String, Class<?>> classes, final URL url) {
        try (InputStream inputStream = url.openStream()) {
            Properties properties = new Properties();
            properties.load(inputStream);
            properties.forEach((k, v) -> {
                String name = (String) k;
                String classPath = (String) v;
                if (StringUtils.isNoneBlank(name, classPath)) {
                    try {
                        loadClass(classes, name, classPath);
                    } catch (ClassNotFoundException e) {
                        throw new IllegalStateException("load extension resources error", e);
                    }
                }
            });
        } catch (IOException e) {
            throw new IllegalStateException("load extension resources error", e);
        }
    }

    private void loadClass(final Map<String, Class<?>> classes, final String name, final String classPath) throws ClassNotFoundException {
        Class<?> subClass = Objects.nonNull(this.classLoader) ? Class.forName(classPath, true, this.classLoader) : Class.forName(classPath);
        if (!clazz.isAssignableFrom(subClass)) {
            throw new IllegalStateException("load extension resources error, " + subClass + " subtype is not of " + clazz);
        }
        if (!subClass.isAnnotationPresent(SPIClass.class)) {
            throw new IllegalStateException("load extension resources error, " + subClass + " without @" + SPIClass.class + " annotation");
        }
        Class<?> oldClass = classes.get(name);
        if (oldClass == null) {
            classes.put(name, subClass);
        } else if (!Objects.equals(oldClass, subClass)) {
            throw new IllegalStateException("load extension resources error, duplicate class " + clazz.getName() + " name " + name + " on " + oldClass.getName() + " or " + subClass.getName());
        }
    }

    public static <T> ExtensionLoader<T> getExtensionLoader(final Class<T> clazz) {
        return getExtensionLoader(clazz, ExtensionLoader.class.getClassLoader());
    }

    public static <T> T getExtension(final Class<T> clazz, String name) {
        return StringUtils.isBlank(name) ? getExtensionLoader(clazz).getDefaultSpiClassInstance() : getExtensionLoader(clazz).getSpiClassInstance(name);
    }

    private static <T> ExtensionLoader<T> getExtensionLoader(Class<T> clazz, ClassLoader classLoader) {
        Objects.requireNonNull(clazz, "extension clazz is null");
        if (!clazz.isInterface()) {
            throw new IllegalArgumentException("extension clazz (" + clazz + ") is not interface");
        }
        if (!clazz.isAnnotationPresent(SPI.class)) {
            throw new IllegalArgumentException("extension clazz (" + clazz + ") without @" + SPI.class + " annotation");
        }
        ExtensionLoader<T> extensionLoader = (ExtensionLoader<T>) LOADERS.get(clazz);
        if (extensionLoader == null) {
            extensionLoader = new ExtensionLoader<T>(clazz, classLoader);
            LOADERS.putIfAbsent(clazz, extensionLoader);
        }
        return extensionLoader;
    }

    public T getDefaultSpiClassInstance() {
        getExtensionClasses();
        if (StringUtils.isBlank(cachedDefaultName)) {
            return null;
        }
        return getSpiClassInstance(cachedDefaultName);
    }

    private T getSpiClassInstance(final String name) {
        if (StringUtils.isBlank(name)) {
            throw new NullPointerException("spi class name is null");
        }
        Holder<Object> objectHolder = cachedInstances.get(name);
        if (objectHolder == null) {
            objectHolder = new Holder<>();
            cachedInstances.putIfAbsent(name, objectHolder);
        }
        Object value = objectHolder.getValue();
        if (value == null) {
            synchronized (cachedInstances) {
                value = objectHolder.getValue();
                if (value == null) {
                    value = createExtension(name);
                    objectHolder.setValue(value);
                }
            }
        }
        return (T) value;
    }

    @SneakyThrows
    private T createExtension(final String name) {
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw new IllegalArgumentException("name is error, name = " + name);
        }
        Object object = spiClassInstances.get(clazz);
        if (object == null) {
            try {
                object = clazz.getDeclaredConstructor().newInstance();
                spiClassInstances.putIfAbsent(clazz, object);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException("Extension instance(name: " + name + ", class: " +
                        clazz + ") could not be instantiated: " + e.getMessage(), e);
            }
        }
        return (T) object;
    }

    private static class Holder<T> {
        private volatile T value;

        public T getValue() {
            return value;
        }

        public void setValue(final T value) {
            this.value = value;
        }
    }
}
