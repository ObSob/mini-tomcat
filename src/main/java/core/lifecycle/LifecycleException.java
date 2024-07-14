package core.lifecycle;

import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;

@Log4j2
public class LifecycleException extends Throwable {

    public LifecycleException(String s) {
        log.error(s);
    }
}
