package core.connector;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

@Log4j2
public class NioEndpoint extends AbstractEndpoint {

    private static final int PORT = 9001;

    private ServerSocketChannel serverSocket;
    @Getter
    private Selector selector;
    @Getter
    private Acceptor acceptor;
    private Poller poller;

    public NioEndpoint(int limit) {
        super(limit);
        this.setHandler(new EchoHandler());
    }

    public void startInternal() throws Exception {
        selector = Selector.open();
        startPollerThread();
        startAcceptorThread();
    }

    private void startPollerThread() {
        // Start poller thread
        poller = new Poller();
        Thread pollerThread = new Thread(poller, "NIO-Poller");
        pollerThread.setDaemon(true);
        pollerThread.start();
    }

    private void startAcceptorThread() {
        acceptor = new Acceptor(this);
        String threadName = "NIO-Acceptor";
        Thread t = new Thread(acceptor, threadName);
        t.start();
    }

    @Override
    public void stopInternal() throws Exception {
        unbind();
    }

    @SneakyThrows
    @Override
    public void bind() {
        serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(PORT), getAcceptCount());
        serverSocket.configureBlocking(true); //mimic APR behavior
    }

    @Override
    public void unbind() throws IOException {
        serverSocket.close();
    }

    @Override
    public SocketChannel serverSocketAccept() throws IOException {
        SocketChannel socketChannel = serverSocket.accept();
        return socketChannel;
    }

    @SneakyThrows
    @Override
    protected void setSocketOptions(SocketChannel socket) {
        socket.configureBlocking(false);
        socket.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        connects.put(socket, new SocketWrapper(this, socket));
        poller.register(socket);
    }

    class Poller implements Runnable {

        private final Selector selector;
        private final LinkedBlockingQueue<PollerEvent> events = new LinkedBlockingQueue<>();

        @SneakyThrows
        public Poller() {
            this.selector = Selector.open();
        }

        @SneakyThrows
        public void register(SocketChannel socket) {
            socket.register(this.selector, SelectionKey.OP_READ);
            PollerEvent pollerEvent = createPollerEvent(socket, SelectionKey.OP_READ);
            addEvent(pollerEvent);
            poller.selector.wakeup();
        }

        public PollerEvent createPollerEvent(SocketChannel socket, int ops) {
            return new PollerEvent(socket, ops);
        }

        public void addEvent(PollerEvent event) {
            this.events.add(event);
            log.debug("event added {}", event);
        }

        @SneakyThrows
        @Override
        public void run() {
            log.debug("poller is running at {}", Thread.currentThread());
            while (true) {
                // 选择准备好的通道（会阻塞）
                this.selector.select();

                // 获取已选择的键集
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();
                    if (key.isReadable()) {
                        processSocket(connects.get((SocketChannel) key.channel()), SocketEvent.OPEN_READ, false);
                    }
                }
            }
        }
    }

    @Data
    @AllArgsConstructor
    static class PollerEvent {
        private SocketChannel socket;
        private int interestOps;
    }

    class NioSocketProcessor extends SocketProcessorBase {

        public NioSocketProcessor(SocketWrapper socketWrapper, SocketEvent event) {
            super(socketWrapper, event);
        }

        @Override
        protected void doRun() {
            getHandler().process(socketWrapper, event);
        }
    }

    @Override
    protected SocketProcessorBase createSocketProcessor(SocketWrapper socketWrapper, SocketEvent event) {
        return new NioSocketProcessor(socketWrapper, event);
    }

    class EchoHandler implements Handler {

        @Override
        public SocketState process(SocketWrapper socket, SocketEvent status) {

            ByteBuffer buffer = ByteBuffer.allocate(128);
            if (socket.read(buffer) == -1) {
                socket.close();
                connects.remove(socket.getSocket());
                log.debug("Connection closed by client {}", socket.getSocket());
                return SocketState.CLOSED;
            }
            buffer.flip();
            log.debug("receive: {}", new String(buffer.array(), 0, buffer.limit(), StandardCharsets.UTF_8));
            socket.write(ByteBuffer.allocate(128).put(buffer));
            return SocketState.ASYNC_IO;
        }
    }
}
