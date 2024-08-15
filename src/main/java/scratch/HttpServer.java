package scratch;

import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

@Log4j2
public class HttpServer {
    public static final String SHOWDOWN_COMMAND = "/shutdown";
    private final int port;
    private ServerSocket serverSocket;
    private volatile boolean running;

    public HttpServer(int port) {
        this.port = port;
    }

    public void await() {
        try {
            serverSocket = new ServerSocket(port);
            log.info("Server started on port {}", port);
            running = true;

            while (running) {
                Socket clientSocket = serverSocket.accept();
                handleClient(clientSocket);
            }
        } catch (IOException e) {
            log.error("Error handling client", e);
        } finally {
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                log.error("Error closing server socket", e);
            }
        }
        log.info("Server has stopped.");
    }

    private void handleClient(Socket clientSocket) {
        String clientInfo = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
        log.info("Handling client connection from: {}", clientInfo);

        try (InputStream is = clientSocket.getInputStream();
             OutputStream os = clientSocket.getOutputStream()) {
            HttpRequest httpRequest = new HttpRequest(is);
            httpRequest.parse();

            if (httpRequest.isEmpty()) {
                log.info("Received empty request from client: {}. Closing connection.", clientInfo);
                return;
            }

            String uri = httpRequest.getRequestURI();
            if (uri == null) {
                log.warn("Received request with null URI from client: {}", clientInfo);
                return;
            }
            if (uri.equals(SHOWDOWN_COMMAND)) {
                running = false;
            } else if (uri.startsWith("/servlet")) {
                HttpResponse httpResponse = new HttpResponse(os);
                httpResponse.setHttpRequest(httpRequest);
                new ServletProcessor().process(httpRequest, httpResponse);
            } else {
                HttpResponse httpResponse = new HttpResponse(os);
                httpResponse.setHttpRequest(httpRequest);
                new StaticResourceProcessor().process(httpRequest, httpResponse);
            }
        } catch (IOException e) {
            log.error("Error handling client request", e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ex) {
                log.error("Error closing client socket", ex);
            }
        }
    }

}

