package scratch;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Path;
import java.util.Locale;

@Log4j2
public class Response implements ServletResponse {
    private static final String WEB_ROOT = "webapp";
    private static final String HTTP_VERSION = "HTTP/1.1";
    private static final String CONTENT_TYPE_HEADER = "Content-Type: ";
    private static final String CONTENT_LENGTH_HEADER = "Content-Length: ";

    @Setter
    @Getter
    private Request request;
    private final OutputStream output;

    public Response(OutputStream output) {
        this.output = output;
    }

    public void sendStaticResource() throws IOException {
        String uri = request.getUri();
        Path resourcePath = getResourcePath(uri);
        if (isResourcePresent(resourcePath)) {
            byte[] bytes = getResourceBytes(resourcePath);
            sendOkResponse(output, bytes, getContentType(uri));
        } else {
            send404Response(output);
        }
    }

    public static boolean isResourcePresent(Path resourcePath) {
        String resourceRelativePath = resourcePath.toString();
        // 使用当前线程的ClassLoader来获取资源的URL
        URL resourceUrl = Thread.currentThread().getContextClassLoader().getResource(resourceRelativePath);
        // 如果URL不为null，说明资源存在
        return resourceUrl != null;
    }

    private Path getResourcePath(String uri) {
        return Path.of(WEB_ROOT, uri);
    }

    private byte[] getResourceBytes(Path resourcePath) throws IOException {
        try (InputStream resource = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath.toString())) {
            if (resource == null) {
                log.error("Resource not found: {}", resourcePath);
                throw new IOException("Resource not found: " + resourcePath);
            }
            return resource.readAllBytes();
        }
    }

    private void sendOkResponse(OutputStream output, byte[] body, String contentType) throws IOException {
        String header = HTTP_VERSION + " 200 OK\r\n" +
                CONTENT_TYPE_HEADER + contentType + "\r\n" +
                CONTENT_LENGTH_HEADER + body.length + "\r\n" +
                "\r\n";
        output.write(header.getBytes());
        output.write(body);
    }

    private void send404Response(OutputStream output) throws IOException {
        String responseBody = "<html><body><h1>404 File Not Found</h1></body></html>";
        String header = HTTP_VERSION + " 404 File Not Found\r\n" +
                CONTENT_TYPE_HEADER + "text/html\r\n" +
                CONTENT_LENGTH_HEADER + responseBody.length() + "\r\n" +
                "\r\n";
        output.write(header.getBytes());
        output.write(responseBody.getBytes());
    }

    private String getContentType(String uri) {
        if (uri.endsWith(".html") || uri.endsWith(".htm")) {
            return "text/html";
        } else if (uri.endsWith(".css")) {
            return "text/css";
        } else if (uri.endsWith(".js")) {
            return "application/javascript";
        } else if (uri.endsWith(".json")) {
            return "application/json";
        } else if (uri.endsWith(".xml")) {
            return "application/xml";
        } else {
            return "application/octet-stream";
        }
    }

    @Override
    public String getCharacterEncoding() {
        return "";
    }

    @Override
    public String getContentType() {
        return "";
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(output, true);
    }

    @Override
    public void setCharacterEncoding(String charset) {

    }

    @Override
    public void setContentLength(int len) {

    }

    @Override
    public void setContentLengthLong(long len) {

    }

    @Override
    public void setContentType(String type) {

    }

    @Override
    public void setBufferSize(int size) {

    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public void flushBuffer() throws IOException {

    }

    @Override
    public void resetBuffer() {

    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public void setLocale(Locale loc) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }
}
