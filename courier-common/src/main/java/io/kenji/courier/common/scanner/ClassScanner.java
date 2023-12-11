package io.kenji.courier.common.scanner;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/9
 **/
public class ClassScanner {
    /**
     * file
     */
    private static final String PROTOCOL_FILE = "file";

    /**
     * jar package
     */
    private static final String PROTOCOL_JAR = "jar";
    /**
     * class file suffix
     */
    private static final String CLASS_FILE_SUFFIX = ".class";

    private static void findAndAddClassesInPackageByFile(String packageName, String packagePath, List<String> classNameList, final boolean recursive) {
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] dirFiles = dir.listFiles(file -> ((recursive && file.isDirectory()) || file.getName().endsWith(".class")));
        Optional.ofNullable(dirFiles).ifPresent(files -> Arrays.stream(files).forEach(file -> {
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), classNameList, recursive);
            } else {
                String className = file.getName().substring(0, file.getName().length() - 6);
                classNameList.add(packageName + "." + className);
            }
        }));
    }

    private static void findAndAddClassesInPackageByJar(String packageName, List<String> classNameList, boolean recursive, String packageDirName, URL url) throws IOException {
        JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.charAt(0) == '/') {
                name = name.substring(1);
            }
            if (name.startsWith(packageDirName)) {
                int idx = name.lastIndexOf('/');
                if (idx != -1) {
                    packageName = name.substring(0, idx).replace('/', '.');
                }
                if ((idx != -1) || recursive) {
                    if (name.endsWith(CLASS_FILE_SUFFIX) && !entry.isDirectory()) {
                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                        classNameList.add(packageName + "." + className);
                    }
                }
            }
        }
    }

    public static List<String> getClassNameList(String packageName) throws IOException {
        List<String> classNameList = new ArrayList<>();
        boolean recursive = true;
        String packageDirName = packageName.replace('.', '/');
        Enumeration<URL> dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
        while (dirs.hasMoreElements()) {
            URL url = dirs.nextElement();
            String protocol = url.getProtocol();
            if (PROTOCOL_FILE.equals(protocol)) {
                String filePath = URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8);
                findAndAddClassesInPackageByFile(packageName, filePath, classNameList, recursive);
            } else if (PROTOCOL_JAR.equals(protocol)) {
                findAndAddClassesInPackageByJar(packageName, classNameList, recursive, packageDirName, url);
            }
        }
        return classNameList;
    }
}
