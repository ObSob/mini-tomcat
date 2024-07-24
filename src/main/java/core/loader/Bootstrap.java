package core.loader;

import com.google.common.collect.Lists;
import core.TomcatProperties;
import core.loader.ClassLoaderFactory.Repository;
import core.loader.ClassLoaderFactory.RepositoryType;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Method;
import java.util.List;



@Log4j2
public class Bootstrap {

    /**
     *              BootstrapClassLoader
     *                      |
     *                  ExtClassLoader
     *                      |
     *                  AppClassLoader
     *                  |           |
     *  CatalinaClassLoader     SharedClassLoader
     *                              |
     *                          WebAppClassLoader
     */
    ClassLoader commonLoader = null;
    ClassLoader catalinaLoader = null;
    ClassLoader sharedLoader = null;

    Object miniTomcatDaemon = null;

    public void init() throws Exception {
        initClassLoaders();

        // load catalina
        Class<?> startupClass = commonLoader.loadClass("core.startup.MiniTomcat");
        Object startupInstance = startupClass.getConstructor().newInstance();
        String methodName = "setParentClassLoader";
        Class<?>[] paramTypes = new Class[1];
        paramTypes[0] = Class.forName("java.lang.ClassLoader");
        Object[] paramValues = new Object[1];
        paramValues[0] = sharedLoader;
        Method method = startupInstance.getClass().getMethod(methodName, paramTypes);
        method.invoke(startupInstance, paramValues);

        miniTomcatDaemon = startupInstance;

        log.trace("daemon loaded, {}", miniTomcatDaemon);
    }

    private void initClassLoaders() {
        try {
            commonLoader = createClassLoader("common", null);
            catalinaLoader = createClassLoader("server", commonLoader);
            sharedLoader = createClassLoader("shared", commonLoader);
        } catch (Throwable t) {
            log.error("Class loader creation threw exception", t);
            System.exit(1);
        }
    }

    private static ClassLoader createClassLoader(String name, ClassLoader parent) throws Exception {
        String prop = TomcatProperties.getProperty(name + ".loader");

        List<Repository> repositories = Lists.newArrayList();
        String[] paths = prop.split(",");

        for (String repository : paths) {
            // Local repository
            if (repository.endsWith("*.jar")) {
                repository = repository.substring(0, repository.length() - "*.jar".length());
                repositories.add(new ClassLoaderFactory.Repository(repository, RepositoryType.GLOB));
            } else if (repository.endsWith(".jar")) {
                repositories.add(new Repository(repository, RepositoryType.JAR));
            } else {
                repositories.add(new Repository(repository, RepositoryType.DIR));
            }

        }
        return ClassLoaderFactory.createClassLoader(repositories, parent);
    }

    @SneakyThrows
    public static void main(String[] args) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.init();
    }
}
