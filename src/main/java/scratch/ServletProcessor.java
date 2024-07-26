package scratch;

import jakarta.servlet.Servlet;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.*;
import java.util.Objects;

@Log4j2
public class ServletProcessor {

    private static final String WEB_ROOT = "webapp";

    public void process(Request request, Response response) {
        String uri = request.getUri();
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
        RequestFacade requestFacade = new RequestFacade(request);
        ResponseFacade responseFacade = new ResponseFacade(response);
        try {
            assert myClass != null;
            servlet = (Servlet) myClass.getConstructor().newInstance();
            servlet.service(requestFacade, responseFacade);
        } catch (Exception e) {
            log.error(e);
        }
    }
}
