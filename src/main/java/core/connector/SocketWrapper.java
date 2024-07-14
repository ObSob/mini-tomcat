package core.connector;

import lombok.Getter;
import lombok.SneakyThrows;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.locks.ReentrantLock;

@Getter
public class SocketWrapper {

    private final ReentrantLock lock;
    private final AbstractEndpoint endpoint;
    private final SocketChannel socket;


    public SocketWrapper(AbstractEndpoint endpoint, SocketChannel socket) {
        this.endpoint = endpoint;
        this.socket = socket;
        lock = new ReentrantLock();
    }

    @SneakyThrows
    public int read(ByteBuffer to) {
        return this.socket.read(to);
    }

    @SneakyThrows
    public void write(ByteBuffer buffer) {
        buffer.rewind();
        socket.write(buffer);
    }

    @SneakyThrows
    public void close() {
        this.socket.close();
        getEndpoint().countDownConnection();
    }

}
