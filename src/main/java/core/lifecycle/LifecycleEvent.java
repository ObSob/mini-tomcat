package core.lifecycle;


import lombok.Getter;

import java.io.Serial;
import java.util.EventObject;

@Getter
public class LifecycleEvent extends EventObject {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String type;
    private final Object data;

    public LifecycleEvent(Lifecycle lifecycle, String type, Object data) {
        super(lifecycle);
        this.type = type;
        this.data = data;
    }

    public Lifecycle getLifecycle() {
        return (Lifecycle) getSource();
    }
}
