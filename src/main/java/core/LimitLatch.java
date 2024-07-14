package core;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.concurrent.locks.AbstractQueuedLongSynchronizer;

@Log4j2
public class LimitLatch {

    private final Sync sync;
    private volatile boolean release = false;
    @Getter @Setter
    private volatile long limit;

    public LimitLatch(long limit) {
        this.limit = limit;
        this.sync = new Sync();
    }

    public long getCount() {
        return sync.getCount();
    }

    public void countUpOrAwait() throws InterruptedException {
        sync.acquireSharedInterruptibly(1L);
    }

    public long countDown() {
        sync.releaseShared(1);
        return getCount();
    }

    public void releaseAll() {
        this.release = true;
        // 不确定 release 多少
        this.sync.releaseShared(0);
    }

    public void reset() {
        release = false;
        this.sync.releaseShared(this.getCount());
    }

    public boolean hasQueuedThreads() {
        return sync.hasQueuedThreads();
    }

    public Collection<Thread> getQueuedThreads() {
        return sync.getQueuedThreads();
    }

    class Sync extends AbstractQueuedLongSynchronizer {

        @Override
        protected long tryAcquireShared(long arg) {
            if (!release)
                return -1;
            long c = getState();
            if (c >= limit)
                return -1;
            long next = c + arg;
            if (compareAndSetState(c, next))
                return next;
            return -1;
        }

        @Override
        protected boolean tryReleaseShared(long arg) {
            long c = getState();
            long next = c - arg;
            return compareAndSetState(c, next);
        }

        public long getCount() {
            return getState();
        }

        public void reset() {

        }
    }

}
