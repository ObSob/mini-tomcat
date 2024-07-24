package core.loader;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Set;

@Log4j2
public class ClassLoaderFactory {

    @SneakyThrows
    public static ClassLoader createClassLoader(List<Repository> repositories, ClassLoader parent) {
        Set<URI> uris = getUris(repositories);
        URL[] urls = uris.stream()
                .map(uri -> {
                    try {
                        return uri.toURL();
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toArray(URL[]::new);
        for (int i = 0; i < urls.length; i++) {
            log.trace("location {} is {}", i, urls[i]);
        }

        return new URLClassLoader(urls, parent);
    }

    private static Set<URI> getUris(List<Repository> repositories) throws IOException {
        Set<URI> uris = Sets.newLinkedHashSet();
        for (var repo : repositories) {
            switch (repo.type) {
                case DIR, JAR -> {
                    File file = new File(repo.getLocation()).getCanonicalFile();
                    uris.add(file.toURI());
                }
                case GLOB -> {
                    File directory = new File(repo.getLocation()).getAbsoluteFile();
                    var files = directory.list();
                    if (files == null) {
                        continue;
                    }
                    for (var path : files) {
                        File file = new File(path);
                        uris.add(file.toURI());
                    }
                }
            }
        }
        return uris;
    }

    @Data
    @AllArgsConstructor
    public static class Repository {
        private String location;
        private RepositoryType type;
    }

    public enum RepositoryType {
        GLOB,
        DIR,
        JAR,
    }
}
