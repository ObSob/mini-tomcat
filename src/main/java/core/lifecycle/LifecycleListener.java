package core.lifecycle;

import java.util.EventListener;

public interface LifecycleListener extends EventListener {

    void lifecycleEvent(LifecycleEvent event);

}
