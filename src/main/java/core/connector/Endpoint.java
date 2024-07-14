package core.connector;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public interface Endpoint {

    void init() throws Exception;

    void start() throws Exception;

    void stop() throws Exception;

    void destroy() throws Exception;

    void bind();

    void unbind() throws IOException;

    SocketChannel serverSocketAccept() throws IOException;
}
