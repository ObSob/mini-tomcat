package core;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.concurrent.locks.AbstractQueuedLongSynchronizer;

@Slf4j
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

    public void countUpOrWait() throws InterruptedException {
        sync.acquireSharedInterruptibly(1L);
    }

    public long countDown() {
        sync.releaseShared(1);
        return getCount();
    }

    public void releaseAll() {
        this.release = true;
        // 不确定 release 多少
        this.sync.release(0);
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
            long next = c - arg;
            if (compareAndSetState(c, next))
                return next;
            return -1;
        }

        @Override
        protected boolean tryReleaseShared(long arg) {
            long c = getState();
            long next = c + 1;
            return compareAndSetState(c, next);
        }

        public long getCount() {
            return getState();
        }

        public void reset() {

        }
    }

}
