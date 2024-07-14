package core.connector;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.nio.channels.SocketChannel;

@Log4j2
public class Acceptor implements Runnable {

    private final AbstractEndpoint endpoint;

    public Acceptor(AbstractEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    @SneakyThrows
    @Override
    public void run() {
        while (true) {
            endpoint.countUpOrAwaitConnection();
            SocketChannel socketChannel = endpoint.serverSocketAccept();
            endpoint.setSocketOptions(socketChannel);
            log.info("Accepted new connection from {}", socketChannel.getRemoteAddress());
            Thread.sleep(1000);
        }
    }

    /**
     *         log.debug("Server started on port {}", PORT);
     *         while (true) {
     *             // 选择准备好的通道
     *             selector.select();
     *
     *             // 获取已选择的键集
     *             Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
     *             while (keyIterator.hasNext()) {
     *                 SelectionKey key = keyIterator.next();
     *                 keyIterator.remove();
     *
     *                 if (key.isAcceptable()) {
     *                     handleAccept(key);
     *                 } else if (key.isReadable()) {
     *                     handleRead((SocketChannel) key.channel());
     *                 }
     *             }
     *         }
     */

}
