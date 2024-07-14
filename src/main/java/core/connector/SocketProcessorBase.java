package core.connector;

import java.util.Objects;
import java.util.concurrent.locks.Lock;

public abstract class SocketProcessorBase implements Runnable {

    protected SocketWrapper socketWrapper;
    protected SocketEvent event;

    public SocketProcessorBase(SocketWrapper socketWrapper, SocketEvent event) {
        reset(socketWrapper, event);
    }

    public void reset(SocketWrapper socketWrapper, SocketEvent event) {
        Objects.requireNonNull(event);
        this.socketWrapper = socketWrapper;
        this.event = event;
    }

    @Override
    public final void run() {
        Lock lock = socketWrapper.getLock();
        lock.lock();
        try {
            doRun();
        } finally {
            lock.unlock();
        }
    }

    protected abstract void doRun();
}
