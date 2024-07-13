package core.lifecycle;

import java.util.List;

/*
 *            start()
 *  -----------------------------
 *  |                           |
 *  | init()                    |
 * NEW -»-- INITIALIZING        |
 * | |           |              |     ------------------«-----------------------
 * | |           |auto          |     |                                        |
 * | |          \|/    start() \|/   \|/     auto          auto         stop() |
 * | |      INITIALIZED --»-- STARTING_PREP --»- STARTING --»- STARTED --»---  |
 * | |         |                                                            |  |
 * | |destroy()|                                                            |  |
 * | --»-----«--    ------------------------«--------------------------------  ^
 * |     |          |                                                          |
 * |     |         \|/          auto                 auto              start() |
 * |     |     STOPPING_PREP ----»---- STOPPING ------»----- STOPPED -----»-----
 * |    \|/                               ^                     |  ^
 * |     |               stop()           |                     |  |
 * |     |       --------------------------                     |  |
 * |     |       |                                              |  |
 * |     |       |    destroy()                       destroy() |  |
 * |     |    FAILED ----»------ DESTROYING ---«-----------------  |
 * |     |                        ^     |                          |
 * |     |     destroy()          |     |auto                      |
 * |     --------»-----------------    \|/                         |
 * |                                 DESTROYED                     |
 * |                                                               |
 * |                            stop()                             |
 * ----»-----------------------------»------------------------------*/
public interface Lifecycle {

    String BEFORE_INIT_EVENT = "before init";
    String AFTER_INIT_EVENT = "after init";
    String BEFORE_START_EVENT = "before start";
    String START_EVENT = "start";
    String AFTER_START_EVENT = "after start";
    String BEFORE_STOP_EVENT = "before stop";
    String STOP_EVENT = "stop";
    String AFTER_STOP_EVENT = "after stop";
    String BEFORE_DESTROY_EVENT = "before destroy";
    String AFTER_DESTROY_EVENT = "after destroy";

    void init() throws LifecycleException;
    void start() throws LifecycleException;
    void stop() throws LifecycleException;
    void destroy() throws LifecycleException;

    LifecycleState getState();

    List<LifecycleListener> getLifecycleListeners();
    void addLifecycleListener(LifecycleListener lifecycleListener);
    void removeLifecycleListener(LifecycleListener lifecycleListener);

}
