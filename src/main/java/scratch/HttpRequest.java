package scratch;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Getter
@Log4j2
public class HttpRequest {

    private final InputStream input;
    private String uri;

    public HttpRequest(InputStream input) throws IOException {
        this.input = input;
    }

    private boolean isEmpty;

    public void parse() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line = reader.readLine();
        if (line == null || line.isEmpty()) {
            isEmpty = true;
            log.info("Received an empty request");
            return;
        }
        String[] parts = line.split("\\s+");
        if (parts.length > 1) {
            uri = parts[1];
        }
        log.info("Parsed request with URI: {}", uri);
    }

    public boolean isEmpty() {
        return isEmpty;
    }

}
