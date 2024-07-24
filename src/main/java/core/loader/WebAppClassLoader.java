package core.loader;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.core.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Log4j2
public class WebAppClassLoader extends ClassLoader {

    private final static ClassLoader appClassLoader = ClassLoader.getSystemClassLoader();
    private final static ClassLoader extClassLoader = appClassLoader.getParent();

    private final Map<String, Class<?>> clazzMap = new HashMap<>();
    private final String resourcePath = WebAppClassLoader.class.getClassLoader().getResource("").getPath();


    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (this) {
            Class<?> clazz;
            clazz = findClassInternal(name);
            if (clazz != null) {
                if (resolve) {
                    resolveClass(clazz);
                }
                return clazz;
            }
            try {
                clazz = extClassLoader.loadClass(name);
                if (clazz != null) {
                    if (resolve) {
                        resolveClass(clazz);
                    }
                    return clazz;
                }
            } catch (ClassNotFoundException ignored) {
            }
        }

        throw new ClassNotFoundException();
    }

    public Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> clazz;
        clazz = findClassInternal(name);
        if (clazz != null) {
            return clazz;
        }
        clazz = super.findClass(name);
        if (clazz != null) {
            return clazz;
        }
        throw new ClassNotFoundException();
    }

    private Class<?> findClassInternal(String name) {
        if (clazzMap.containsKey(name)) {
            return clazzMap.get(name);
        }
        try {
            URL resource = this.getClass().getClassLoader().getResource("");
            assert resource != null;
            File root = new File(resource.toURI());
            String pathName = name.replace(".", "/") + ".class";
            Optional<Path> classPath = findCassFile(root.toPath(), pathName);
            if (classPath.isEmpty()) {
                return null;
            }
            String className = getClassName(classPath.get());
            byte[] classBytes = Files.readAllBytes(classPath.get());
            Class<?> clazz = defineClass(className, classBytes, 0, classBytes.length);
            clazzMap.put(className, clazz);
            log.info("class {} loaded", clazz.getCanonicalName());
            return clazz;
        } catch (IOException | URISyntaxException ignored) {
        }

        return clazzMap.get(name);
    }

    private Optional<Path> findCassFile(Path root, String filename) throws IOException {
        try (Stream<Path> stream = Files.walk(root)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals(filename))
                    .findFirst();
        }
    }

    private String getClassName(Path filePath) {
        // 获取文件名
        String fileName = filePath.toString();
        if (fileName.startsWith(resourcePath)) {
            fileName = fileName.substring(resourcePath.length());
        }
        // 去掉后缀
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1) {
            fileName = fileName.substring(0, dotIndex);
        }
        return fileName.replace("/", ".");
    }

    public static void main(String[] args) {
        WebAppClassLoader classLoader = new WebAppClassLoader();
        try {
            // 尝试加载名为 "ExampleClass" 的类
            Class<?> clazz = classLoader.loadClass("ExampleClass");

            // 打印加载的类，确认加载成功
            System.out.println("Loaded class: " + clazz.getName());

            // 创建 ExampleClass 的实例并调用其方法（假设有一个无参构造函数和一个名为 printMessage 的方法）
            Object instance = clazz.getDeclaredConstructor().newInstance();
            clazz.getMethod("printMessage").invoke(instance);

        } catch (Exception e) {
            log.error(e);
        }
    }

}
