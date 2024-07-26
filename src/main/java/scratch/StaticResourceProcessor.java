package scratch;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
public class StaticResourceProcessor {

    public void process(Request request, Response response) {
        try {
            response.sendStaticResource();
        } catch (IOException e) {
            log.error(e);
        }
    }
}
