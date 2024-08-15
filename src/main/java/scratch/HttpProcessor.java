package scratch;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

@Log4j2
public class HttpProcessor {

    private final HttpConnector connector;

    public HttpProcessor(HttpConnector connector) {
        this.connector = connector;
    }

    public void process(Socket clientSocket) {
        log.trace("process");
        String clientInfo = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
        log.info("Handling client connection from: {}", clientInfo);

        try (InputStream input = clientSocket.getInputStream();
             OutputStream output = clientSocket.getOutputStream()) {
            HttpRequest httpRequest = new HttpRequest(input);
            HttpResponse httpResponse = new HttpResponse(output);
            httpResponse.setHttpRequest(httpRequest);

            httpResponse.setHeader("Server", "mini-tomcat server");
            httpRequest.parse();

            if (httpRequest.isEmpty()) {
                log.info("Received empty request from client: {}. Closing connection.", clientInfo);
                clientSocket.close();
                return;
            }

            String uri = httpRequest.getRequestURI();
            if (uri == null) {
                log.warn("Received request with null URI from client: {}", clientInfo);
                return;
            }

            if (uri.startsWith("/servlet/")) {
                new ServletProcessor().process(httpRequest, httpResponse);
            } else {
                new StaticResourceProcessor().process(httpRequest, httpResponse);
            }
        } catch (IOException e) {
            log.error("Error handling client request", e);
        } finally {
            try {
                clientSocket.close();
                log.info("{} closed", clientInfo);
            } catch (IOException ex) {
                log.error("Error closing client socket", ex);
            }
        }

    }

}
