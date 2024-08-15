package scratch;

import jakarta.servlet.Servlet;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.*;
import java.util.Objects;

@Log4j2
public class ServletProcessor {

    private static final String WEB_ROOT = "webapp";

    public void process(HttpRequest httpRequest, HttpResponse httpResponse) {
        String uri = httpRequest.getRequestURI();
        String servletName = uri.substring(uri.lastIndexOf("/") + 1);
        URLClassLoader loader = null;
        try {
            URL url = Objects.requireNonNull(getClass().getClassLoader().getResource(WEB_ROOT + "/")).toURI().toURL();
            loader = new URLClassLoader(new URL[]{url});
        } catch (Exception e) {
            log.error(e);
        }

        Class<?> myClass = null;
        try {
            assert loader != null;
            myClass = loader.loadClass(servletName);
        } catch (Exception e) {
            log.error(e);
        }

        try {
            loader.close();
        } catch (IOException e) {
            log.error(e);
        }

        Servlet servlet;
        HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(httpRequest);
        HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(httpResponse);
        try {
            assert myClass != null;
            servlet = (Servlet) myClass.getConstructor().newInstance();
            servlet.service(requestWrapper, responseWrapper);
        } catch (Exception e) {
            log.error(e);
        }
    }
}
