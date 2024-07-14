package core.connector;

import core.LimitLatch;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

@Log4j2
public abstract class AbstractEndpoint implements Endpoint {

    protected final LimitLatch limitLatch;
    private ServerSocketChannel serverSocket;
    @Getter
    protected final Map<SocketChannel, SocketWrapper> connects = new ConcurrentHashMap<>();
    @Getter
    @Setter
    private Handler handler;

    public AbstractEndpoint(int limit) {
        this.limitLatch = new LimitLatch(limit);
    }

    @Override
    public void init() throws Exception {
    }

    @Override
    public void start() throws Exception {
        bind();
        startInternal();
        limitLatch.releaseAll();
    }

    @Override
    public void stop() throws Exception {
        unbind();
        stopInternal();
    }

    @Override
    public void destroy() throws Exception {
    }

    protected int getAcceptCount() {
        return (int) limitLatch.getCount();
    }

    public abstract void startInternal() throws Exception;

    public abstract void stopInternal() throws Exception;

    public abstract void bind();

    public abstract void unbind() throws IOException;

    protected abstract void setSocketOptions(SocketChannel socket);

    public interface Handler {

        /**
         * Different types of socket states to react upon.
         */
        enum SocketState {
            // TODO Add a new state to the AsyncStateMachine and remove
            //      ASYNC_END (if possible)
            OPEN, CLOSED, LONG, ASYNC_END, SENDFILE, UPGRADING, UPGRADED, ASYNC_IO, SUSPENDED
        }

        /**
         * Process the provided socket with the given current status.
         *
         * @param socket The socket to process
         * @param status The current socket status
         * @return The state of the socket after processing
         */
        SocketState process(SocketWrapper socket,
                            SocketEvent status);

    }

    public boolean processSocket(SocketWrapper socketWrapper,
                                 SocketEvent event, boolean dispatch) {
        try {

            SocketProcessorBase sc = createSocketProcessor(socketWrapper, event);
            Executor executor = getExecutor();
            if (dispatch && executor != null) {
                executor.execute(sc);
            } else {
                sc.run();
            }
        } catch (Exception e) {
            log.error(e);
            return false;
        }
        return true;
    }

    protected abstract SocketProcessorBase createSocketProcessor(SocketWrapper socketWrapper, SocketEvent event);

    @Getter @Setter
    private Executor executor = null;


    protected void countUpOrAwaitConnection() throws InterruptedException {
        this.limitLatch.countUpOrAwait();
    }

    protected void countDownConnection() {
        this.limitLatch.countDown();
    }
}
