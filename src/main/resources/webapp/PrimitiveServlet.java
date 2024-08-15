import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PrimitiveServlet implements Servlet {

    private ServletConfig config;

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.config = config;
        System.out.println("init");
    }

    @Override
    public ServletConfig getServletConfig() {
        return this.config;
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        if (req instanceof HttpServletRequest && res instanceof HttpServletResponse) {
            HttpServletRequest httpReq = (HttpServletRequest) req;
            HttpServletResponse httpRes = (HttpServletResponse) res;

            // 设置HTTP状态码
            httpRes.setStatus(HttpServletResponse.SC_OK);

            // 设置响应头
            httpRes.setHeader("Content-Type", "text/html;charset=UTF-8");
            httpRes.setHeader("Server", "PrimitiveServlet");
            httpRes.setHeader("X-Powered-By", "Java Servlet");

            // 设置日期头
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
            httpRes.setHeader("Date", dateFormat.format(new Date()));

            // 准备响应体
            String responseBody = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head><title>PrimitiveServlet Response</title></head>\n" +
                    "<body>\n" +
                    "<h1>Response from PrimitiveServlet</h1>\n" +
                    "<p>Request method: " + httpReq.getMethod() + "</p>\n" +
                    "<p>Request URI: " + httpReq.getRequestURI() + "</p>\n" +
                    "</body>\n" +
                    "</html>";

            // 设置Content-Length头
            httpRes.setHeader("Content-Length", String.valueOf(responseBody.getBytes("UTF-8").length));

            // 获取输出流并写入响应体
            PrintWriter out = httpRes.getWriter();
            out.write(responseBody);
            out.flush();
        } else {
            // 如果不是 HTTP 请求/响应，则发送简单的文本响应
            res.setContentType("text/plain");
            PrintWriter out = res.getWriter();
            out.println("From PrimitiveServlet");
            out.flush();
        }

        System.out.println("PrimitiveServlet service");
    }

    @Override
    public String getServletInfo() {
        return "";
    }

    @Override
    public void destroy() {
        System.out.println("destroy");
    }
}
