package scratch;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Log4j2
public class HttpRequest implements HttpServletRequest {

    private final InputStream input;
    private boolean isEmpty;

    // 请求行部分
    private String method;
    @Getter
    private String requestURI;
    private String version;

    // 请求头部分
    private final Map<String, String> headers;

    // 请求体部分
    private String body;

    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    private String characterEncoding;

    public HttpRequest(InputStream input) throws IOException {
        this.input = input;
        this.headers = new ConcurrentHashMap<>();
    }

    // 解析InputStream填充HttpRequest
    public void parse() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

        // 1. 读取请求行
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            log.error("Empty request line");
            return;
        }
        String[] requestLineParts = requestLine.split("\\s+");
        if (requestLineParts.length != 3) {
            throw new IOException("Invalid request line");
        }
        this.method = requestLineParts[0];
        this.requestURI = requestLineParts[1];
        this.version = requestLineParts[2];

        // 2. 读取请求头
        String headerLine;
        while (!(headerLine = reader.readLine()).isEmpty()) {
            String[] headerParts = headerLine.split(":\\s*", 2);
            if (headerParts.length == 2) {
                this.headers.put(headerParts[0], headerParts[1]);
            }
        }

        // 3. 读取请求体
        String contentLengthHeader = this.headers.get("Content-Length");
        if (contentLengthHeader != null) {
            int contentLength = Integer.parseInt(contentLengthHeader);
            char[] bodyChars = new char[contentLength];
            int ignore = reader.read(bodyChars, 0, contentLength);
            this.body = new String(bodyChars);
        }
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }

    @Override
    public String getRequestId() {
        return "";
    }

    @Override
    public String getProtocolRequestId() {
        return "";
    }

    @Override
    public ServletConnection getServletConnection() {
        return null;
    }

    @Override
    public String getAuthType() {
        return "";
    }

    @Override
    public Cookie[] getCookies() {
        return new Cookie[0];
    }

    @Override
    public long getDateHeader(String name) {
        return 0;
    }

    @Override
    public String getHeader(String name) {
        return headers.get(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        String value = headers.get(name);
        return value != null ? Collections.enumeration(Collections.singletonList(value)) : Collections.emptyEnumeration();
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return null;
    }

    @Override
    public int getIntHeader(String name) {
        return 0;
    }

    @Override
    public String getPathInfo() {
        return "";
    }

    @Override
    public String getPathTranslated() {
        return "";
    }

    @Override
    public String getContextPath() {
        return "";
    }

    @Override
    public String getQueryString() {
        return "";
    }

    @Override
    public String getRemoteUser() {
        return "";
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return "";
    }

    @Override
    public StringBuffer getRequestURL() {
        return null;
    }

    @Override
    public String getServletPath() {
        return "";
    }

    @Override
    public HttpSession getSession(boolean create) {
        return null;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public String changeSessionId() {
        return "";
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(String username, String password) throws ServletException {

    }

    @Override
    public void logout() throws ServletException {

    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return List.of();
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        return null;
    }


    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public String getCharacterEncoding() {
        return characterEncoding;
    }

    @Override
    public void setCharacterEncoding(String env) {
        this.characterEncoding = env;
    }

    @Override
    public int getContentLength() {
        String contentLength = getHeader("Content-Length");
        return contentLength != null ? Integer.parseInt(contentLength) : -1;
    }

    @Override
    public long getContentLengthLong() {
        String contentLength = getHeader("Content-Length");
        return contentLength != null ? Long.parseLong(contentLength) : -1L;
    }

    @Override
    public String getContentType() {
        return getHeader("Content-Type");
    }

    private Map<String, String[]> parameters;

    private void parseParameters() {
        if (parameters == null) {
            parameters = new HashMap<>();
            String queryString = getQueryString();
            if (queryString != null) {
                for (String pair : queryString.split("&")) {
                    String[] keyValue = pair.split("=");
                    String key = keyValue[0];
                    String value = keyValue.length > 1 ? keyValue[1] : "";
                    String[] values = parameters.get(key);
                    if (values == null) {
                        parameters.put(key, new String[]{value});
                    } else {
                        String[] newValues = Arrays.copyOf(values, values.length + 1);
                        newValues[values.length] = value;
                        parameters.put(key, newValues);
                    }
                }
            }
        }
    }

    @Override
    public String getParameter(String name) {
        parseParameters();
        String[] values = parameters.get(name);
        return values != null ? values[0] : null;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        parseParameters();
        return Collections.enumeration(parameters.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        parseParameters();
        return parameters.get(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        parseParameters();
        return Collections.unmodifiableMap(parameters);
    }



    @Override
    public String getProtocol() {
        return version;
    }

    @Override
    public String getScheme() {
        return requestURI.startsWith("https") ? "https" : "http";
    }

    @Override
    public String getServerName() {
        String host = getHeader("Host");
        if (host != null) {
            int colonIndex = host.indexOf(':');
            if (colonIndex != -1) {
                return host.substring(0, colonIndex);
            }
            return host;
        }
        return null;
    }

    @Override
    public int getServerPort() {
        String host = getHeader("Host");
        if (host != null) {
            int colonIndex = host.indexOf(':');
            if (colonIndex != -1) {
                return Integer.parseInt(host.substring(colonIndex + 1));
            }
        }
        return getScheme().equals("https") ? 443 : 80;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public String getRemoteAddr() {
        return "127.0.0.1"; // 假设请求来自本地
    }

    @Override
    public String getRemoteHost() {
        return "localhost"; // 假设请求来自本地
    }

    @Override
    public void setAttribute(String name, Object o) {
        attributes.put(name, o);
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    @Override
    public Locale getLocale() {
        String acceptLanguage = getHeader("Accept-Language");
        if (acceptLanguage != null) {
            String[] languages = acceptLanguage.split(",");
            if (languages.length > 0) {
                String[] parts = languages[0].trim().split("-");
                return new Locale(parts[0], parts.length > 1 ? parts[1] : "");
            }
        }
        return Locale.getDefault();
    }

    @Override
    public Enumeration<Locale> getLocales() {
        List<Locale> locales = new ArrayList<>();
        String acceptLanguage = getHeader("Accept-Language");
        if (acceptLanguage != null) {
            String[] languages = acceptLanguage.split(",");
            for (String language : languages) {
                String[] parts = language.trim().split("-");
                locales.add(new Locale(parts[0], parts.length > 1 ? parts[1] : ""));
            }
        }
        if (locales.isEmpty()) {
            locales.add(Locale.getDefault());
        }
        return Collections.enumeration(locales);
    }

    @Override
    public boolean isSecure() {
        return getScheme().equals("https");
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        // 这里需要实现RequestDispatcher，暂时返回null
        return null;
    }

    @Override
    public int getRemotePort() {
        return 0; // 假设无法获取远程端口
    }

    @Override
    public String getLocalName() {
        return "localhost"; // 假设服务器名称为localhost
    }

    @Override
    public String getLocalAddr() {
        return "127.0.0.1"; // 假设服务器地址为127.0.0.1
    }

    @Override
    public int getLocalPort() {
        return 8080; // 假设服务器端口为8080
    }


}
