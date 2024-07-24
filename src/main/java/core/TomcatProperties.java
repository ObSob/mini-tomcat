package core;

import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.net.URI;
import java.util.Objects;
import java.util.Properties;

@Log4j2
public class TomcatProperties {

    private static final Properties properties = new Properties();

    static {
        loadProperties();
    }

    private static void loadProperties() {
        try {
            InputStream is = null;
            String configUrl = System.getProperty("tomcat.config");
            if (configUrl != null) {
                is = new URI(configUrl).toURL().openStream();
            }
            if (is == null) {
                is = Objects.requireNonNull(TomcatProperties.class.getClassLoader().getResource("tomcat.properties")).openStream();
            }

            properties.load(is);
            log.debug("配置文件已加载, {}", properties);
        } catch (Exception e) {
            log.error("配置文件加载失败", e);
        }

    }

    public static String getProperty(String name) {
        return properties.getProperty(name);
    }

    public static void main(String[] args) {
        String property = TomcatProperties.getProperty("tomcat.config");
        log.info("get property, {}", property);
    }

}
