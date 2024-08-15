package scratch;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
public class StaticResourceProcessor {

    public void process(HttpRequest httpRequest, HttpResponse httpResponse) {
        try {
            httpResponse.sendStaticResource();
        } catch (IOException e) {
            log.error(e);
        }
    }
}
