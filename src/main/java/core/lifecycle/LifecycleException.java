package core.lifecycle;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LifecycleException extends Throwable {

    public LifecycleException(String s) {
        log.error(s);
    }
}
