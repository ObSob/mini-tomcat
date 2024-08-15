package scratch;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

@Log4j2
public class HttpResponse implements HttpServletResponse {
    // the default buffer size
    @Getter
    @Setter
    private int bufferSize = 1024;
    @Getter
    @Setter
    HttpRequest httpRequest;
    OutputStream output;
    PrintWriter writer;
    protected byte[] buffer = new byte[bufferSize];
    protected int bufferCount = 0;
    @Getter
    protected boolean committed = false;
    /**
     * The actual number of bytes written to this Response.
     */
    protected int contentCount = 0;
    /**
     * The content length associated with this Response.
     */
    @Getter
    @Setter
    protected int contentLength = -1;
    /**
     * The content type associated with this Response.
     */
    @Getter
    @Setter
    protected String contentType = null;
    /**
     * The character encoding associated with this Response.
     */
    @Getter
    @Setter
    protected String characterEncoding = "utf-8";

    /**
     * The set of Cookies associated with this Response.
     */
    protected ArrayList<Cookie> cookies = new ArrayList<>();
    /**
     * The HTTP headers explicitly added via addHeader(), but not including
     * those to be added with setContentLength(), setContentType(), and so on.
     * This collection is keyed by the header name, and the elements are
     * ArrayLists containing the associated values that have been set.
     */
    protected Map<String, List<String>> headers = new HashMap<>();
    /**
     * The date format we will use for creating date headers.
     */
    @Getter
    @Setter
    protected Locale locale = Locale.CHINA;
    protected final SimpleDateFormat format =
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.CHINA);
    /**
     * The error message set by <code>sendError()</code>.
     */
    protected String message = getStatusMessage(HttpServletResponse.SC_OK);
    /**
     * The HTTP status code associated with this Response.
     */
    @Setter
    @Getter
    protected int status = HttpServletResponse.SC_OK;

    public HttpResponse(OutputStream output) {
        this.output = output;
    }

    public void sendStaticResource() throws IOException {
        String uri = httpRequest.getRequestURI();
        Path resourcePath = getResourcePath(uri);
        if (isResourcePresent(resourcePath)) {
            byte[] bytes = getResourceBytes(resourcePath);
            String result;
            if (uri.endsWith(".html") || uri.endsWith(".htm")) {
                result = "text/html";
            } else if (uri.endsWith(".css")) {
                result = "text/css";
            } else if (uri.endsWith(".js")) {
                result = "application/javascript";
            } else if (uri.endsWith(".json")) {
                result = "application/json";
            } else if (uri.endsWith(".xml")) {
                result = "application/xml";
            } else {
                result = "application/octet-stream";
            }
            sendOkResponse(output, bytes, result);
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
        return Path.of("webapp", uri);
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
        String header = "HTTP/1.1" + " 200 OK\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + body.length + "\r\n" +
                "\r\n";
        output.write(header.getBytes());
        output.write(body);
    }

    private void send404Response(OutputStream output) throws IOException {
        String responseBody = "<html><body><h1>404 File Not Found</h1></body></html>";
        String header = "HTTP/1.1" + " 404 File Not Found\r\n" +
                "Content-Type: " + "text/html\r\n" +
                "Content-Length: " + responseBody.length() + "\r\n" +
                "\r\n";
        output.write(header.getBytes());
        output.write(responseBody.getBytes());
    }

    /**
     * Returns a default status message for the specified HTTP status code.
     *
     * @param status The status code for which a message is desired
     */
    protected String getStatusMessage(int status) {
        return switch (status) {
            case SC_OK -> ("OK");
            case SC_ACCEPTED -> ("Accepted");
            case SC_BAD_GATEWAY -> ("Bad Gateway");
            case SC_BAD_REQUEST -> ("Bad Request");
            case SC_CONFLICT -> ("Conflict");
            case SC_CONTINUE -> ("Continue");
            case SC_CREATED -> ("Created");
            case SC_EXPECTATION_FAILED -> ("Expectation Failed");
            case SC_FORBIDDEN -> ("Forbidden");
            case SC_GATEWAY_TIMEOUT -> ("Gateway Timeout");
            case SC_GONE -> ("Gone");
            case SC_HTTP_VERSION_NOT_SUPPORTED -> ("HTTP Version Not Supported");
            case SC_INTERNAL_SERVER_ERROR -> ("Internal Server Error");
            case SC_LENGTH_REQUIRED -> ("Length Required");
            case SC_METHOD_NOT_ALLOWED -> ("Method Not Allowed");
            case SC_MOVED_PERMANENTLY -> ("Moved Permanently");
            case SC_MOVED_TEMPORARILY -> ("Moved Temporarily");
            case SC_MULTIPLE_CHOICES -> ("Multiple Choices");
            case SC_NO_CONTENT -> ("No Content");
            case SC_NON_AUTHORITATIVE_INFORMATION -> ("Non-Authoritative Information");
            case SC_NOT_ACCEPTABLE -> ("Not Acceptable");
            case SC_NOT_FOUND -> ("Not Found");
            case SC_NOT_IMPLEMENTED -> ("Not Implemented");
            case SC_NOT_MODIFIED -> ("Not Modified");
            case SC_PARTIAL_CONTENT -> ("Partial Content");
            case SC_PAYMENT_REQUIRED -> ("Payment Required");
            case SC_PRECONDITION_FAILED -> ("Precondition Failed");
            case SC_PROXY_AUTHENTICATION_REQUIRED -> ("Proxy Authentication Required");
            case SC_REQUEST_ENTITY_TOO_LARGE -> ("Request Entity Too Large");
            case SC_REQUEST_TIMEOUT -> ("Request Timeout");
            case SC_REQUEST_URI_TOO_LONG -> ("Request URI Too Long");
            case SC_REQUESTED_RANGE_NOT_SATISFIABLE -> ("Requested Range Not Satisfiable");
            case SC_RESET_CONTENT -> ("Reset Content");
            case SC_SEE_OTHER -> ("See Other");
            case SC_SERVICE_UNAVAILABLE -> ("Service Unavailable");
            case SC_SWITCHING_PROTOCOLS -> ("Switching Protocols");
            case SC_UNAUTHORIZED -> ("Unauthorized");
            case SC_UNSUPPORTED_MEDIA_TYPE -> ("Unsupported Media Type");
            case SC_USE_PROXY -> ("Use Proxy");
            case 207 ->       // WebDAV
                    ("Multi-Status");
            case 422 ->       // WebDAV
                    ("Unprocessable Entity");
            case 423 ->       // WebDAV
                    ("Locked");
            case 507 ->       // WebDAV
                    ("Insufficient Storage");
            default -> ("HTTP Response Status " + status);
        };
    }

    private class BufferedServletOutputStream extends ServletOutputStream {
        private final ByteArrayOutputStream buffer;

        public BufferedServletOutputStream() {
            this.buffer = new ByteArrayOutputStream();
        }

        @Override
        public void write(int b) throws IOException {
            buffer.write(b);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            throw new UnsupportedOperationException("setWriteListener is not supported");
        }

        private void writeHeaders() throws IOException {
            // Write status line
            String statusLine = "HTTP/1.1 " + status + " " + getStatusMessage(status) + "\r\n";
            output.write(statusLine.getBytes());

            // Write headers
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                String headerName = entry.getKey();
                for (String headerValue : entry.getValue()) {
                    String headerLine = headerName + ": " + headerValue + "\r\n";
                    output.write(headerLine.getBytes());
                }
            }

            // Write content type and length
            if (contentType != null) {
                String contentTypeLine = "Content-Type: " + contentType + "\r\n";
                output.write(contentTypeLine.getBytes());
            }
            if (contentLength >= 0) {
                String contentLengthLine = "Content-Length: " + contentLength + "\r\n";
                output.write(contentLengthLine.getBytes());
            }

            // End of headers
            output.write("\r\n".getBytes());
        }

        @Override
        public void flush() throws IOException {
            if (!committed) {
                writeHeaders();
                committed = true;
            }
            buffer.writeTo(output);
            buffer.reset();
        }
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (writer != null) {
            throw new IllegalStateException("getWriter() has already been called on this response.");
        }
        return new BufferedServletOutputStream();
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            writer = new PrintWriter(new OutputStreamWriter(getOutputStream(), getCharacterEncoding()), true);
        }
        return writer;
    }

    @Override
    public void setContentLengthLong(long len) {
        this.contentLength = (int) len;
        setHeader("Content-Length", String.valueOf(len));
    }

    @Override
    public void flushBuffer() throws IOException {
        //committed = true;
        if (bufferCount > 0) {
            try {
                output.write(buffer, 0, bufferCount);
            } finally {
                bufferCount = 0;
            }
        }
    }

    @Override
    public void resetBuffer() {
        if (committed) {
            throw new IllegalStateException("Cannot reset buffer - response is already committed");
        }
        bufferCount = 0;
    }


    @Override
    public void reset() {
        resetBuffer();
        cookies.clear();
        headers.clear();
        status = HttpServletResponse.SC_OK;
        message = getStatusMessage(status);
    }

    @Override
    public boolean containsHeader(String name) {
        return headers.containsKey(name.toLowerCase());
    }

    @Override
    public String encodeURL(String url) {
        // URL encoding not implemented
        return url;
    }

    @Override
    public String encodeRedirectURL(String url) {
        // URL encoding not implemented
        return url;
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        if (committed) {
            throw new IllegalStateException("Cannot send error - response is already committed");
        }
        setStatus(sc);
        this.message = msg;
        // Implement error page generation here
    }

    @Override
    public void sendError(int sc) throws IOException {
        sendError(sc, getStatusMessage(sc));
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        if (committed) {
            throw new IllegalStateException("Cannot send redirect - response is already committed");
        }
        setStatus(HttpServletResponse.SC_FOUND);
        setHeader("Location", location);
    }

    @Override
    public void setDateHeader(String name, long date) {
        setHeader(name, format.format(new Date(date)));
    }

    @Override
    public void addDateHeader(String name, long date) {
        addHeader(name, format.format(new Date(date)));
    }

    @Override
    public void setHeader(String name, String value) {
        List<String> values = new ArrayList<>();
        values.add(value);
        headers.put(name.toLowerCase(), values);
    }

    @Override
    public void addHeader(String name, String value) {
        headers.computeIfAbsent(name.toLowerCase(), k -> new ArrayList<>()).add(value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        setHeader(name, String.valueOf(value));
    }

    @Override
    public void addIntHeader(String name, int value) {
        addHeader(name, String.valueOf(value));
    }

    @Override
    public String getHeader(String name) {
        List<String> values = headers.get(name.toLowerCase());
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return headers.getOrDefault(name.toLowerCase(), Collections.emptyList());
    }

    @Override
    public Collection<String> getHeaderNames() {
        return headers.keySet();
    }

    @Override
    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

}
