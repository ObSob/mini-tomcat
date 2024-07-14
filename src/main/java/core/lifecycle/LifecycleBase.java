package core.lifecycle;

import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Log4j2
public abstract class LifecycleBase implements Lifecycle {

    protected volatile LifecycleState state = LifecycleState.NEW;

    private final List<LifecycleListener> lifecycleListeners = new ArrayList<>();

    protected abstract void initInternal();

    protected abstract void startInternal();

    protected abstract void stopInternal();

    protected abstract void destroyInternal();

    @Override
    public final synchronized void init() throws LifecycleException {
        if (!state.equals(LifecycleState.NEW)) {
            invalidTransition(Lifecycle.BEFORE_INIT_EVENT);
        }
        try {
            setStateInternal(LifecycleState.INITIALIZING, null, false);
            initInternal();
            setStateInternal(LifecycleState.INITIALIZED, null, false);
        } catch (LifecycleException e) {
            setStateInternal(LifecycleState.FAILED, null, false);
            log.error(e.getLocalizedMessage());
        }
    }

    @Override
    public void start() throws LifecycleException {
        if (!List.of(LifecycleState.STARTING_PREPARE, LifecycleState.STARTING, LifecycleState.STARTED).contains(this.state)) {
            log.debug("already stated, from {}", this.state);
        }

        if (this.state.equals(LifecycleState.NEW)) {
            init();
        } else if (this.state.equals((LifecycleState.FAILED))) {
            stop();
        }

        if (!List.of(LifecycleState.INITIALIZED, LifecycleState.STOPPED).contains(this.state)) {
            invalidTransition(BEFORE_START_EVENT);
        }

        try {
            setStateInternal(LifecycleState.STARTING_PREPARE, null, false);
            startInternal();
            if (state.equals(LifecycleState.FAILED)) {
                stop();
            } else if (!state.equals(LifecycleState.STARTING)) {
                invalidTransition(AFTER_START_EVENT);
            } else {
                setStateInternal(LifecycleState.STARTED, null, false);
            }
        } catch (LifecycleException e) {
            setStateInternal(LifecycleState.FAILED, null, false);
            log.error(e.getLocalizedMessage());
        }
    }

    @Override
    public void stop() throws LifecycleException {
        if (List.of(LifecycleState.STOPPING_PREPARE, LifecycleState.STOPPING, LifecycleState.STOPPED).contains(this.state)) {
            log.debug("already stopped, from {}", this.state);
        }

        if (!List.of(LifecycleState.NEW, LifecycleState.STARTED, LifecycleState.FAILED).contains(this.state)) {
            invalidTransition(BEFORE_STOP_EVENT);
        }

        if (LifecycleState.NEW.equals(this.state)) {
            return;
        }

        try {
            if (LifecycleState.FAILED.equals(this.state)) {
                fireLifecycleEvent(BEFORE_STOP_EVENT, null);
            } else {
                setStateInternal(LifecycleState.STOPPING_PREPARE, null, false);
            }

            stopInternal();

            if (!List.of(LifecycleState.STOPPING, LifecycleState.FAILED).contains(this.state)) {
                invalidTransition(AFTER_STOP_EVENT);
            }
        } catch (LifecycleException e) {
            setStateInternal(LifecycleState.FAILED, null, false);
            log.error(e.getLocalizedMessage());
        }
    }

    @Override
    public void destroy() throws LifecycleException {
        if (LifecycleState.FAILED.equals(this.state)) {
            try {
                stop();
            } catch (LifecycleException e) {
                log.error(e.getLocalizedMessage());
            }
        }

        if (List.of(LifecycleState.DESTROYING, LifecycleState.DESTROYED).contains(this.state)) {
            log.debug("already destroy, from {}", this.state);
        }

        try {
            setStateInternal(LifecycleState.DESTROYING, null, false);
            destroyInternal();
            setStateInternal(LifecycleState.DESTROYED, null, false);
        } catch (LifecycleException e) {
            setStateInternal(LifecycleState.FAILED, null, false);
            log.error(e.getLocalizedMessage());
        }
    }

    private synchronized void setStateInternal(LifecycleState nextState, Object data, boolean check) throws LifecycleException {
        if (check) {
            checkState(nextState);
        }
        log.debug("state from {} to {}", this.state, nextState);
        this.state = nextState;
        String lifeEvent = nextState.getLifecycleEvent();
        if (lifeEvent != null) {
            fireLifecycleEvent(lifeEvent, data);
        }
    }

    private void checkState(LifecycleState nextState) throws LifecycleException {
        boolean valid = (nextState == LifecycleState.FAILED
                || (this.state == LifecycleState.STARTING_PREPARE && nextState == LifecycleState.STARTING)
                || (this.state == LifecycleState.STOPPING_PREPARE && nextState == LifecycleState.STOPPED));
        if (!valid) {
            invalidTransition(nextState.name());
        }
    }

    public void fireLifecycleEvent(String type, Object data) {
        LifecycleEvent lifecycleEvent = new LifecycleEvent(this, type, data);
        lifecycleListeners.forEach(
                listener -> listener.lifecycleEvent(lifecycleEvent)
        );
    }

    private void invalidTransition(String event) throws LifecycleException {
        throw new LifecycleException("Invalid transition from " + state + " on " + event);
    }

    @Override
    public LifecycleState getState() {
        return this.state;
    }

    @Override
    public List<LifecycleListener> getLifecycleListeners() {
        return Collections.unmodifiableList(this.lifecycleListeners);
    }

    @Override
    public void addLifecycleListener(LifecycleListener lifecycleListener) {
        this.lifecycleListeners.add(lifecycleListener);
    }

    @Override
    public void removeLifecycleListener(LifecycleListener lifecycleListener) {
        this.lifecycleListeners.remove(lifecycleListener);
    }
}
