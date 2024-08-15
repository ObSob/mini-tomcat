package scratch;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class Bootstrap {

    public static void main(String[] args) {
        HttpConnector connector = new HttpConnector();
        Thread connectorThread = connector.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down...");
            connector.stop();
        }));

        try {
            connectorThread.join();
            log.info("Server stopped.");
        } catch (InterruptedException e) {
            log.error("Main thread interrupted", e);
        }
    }
}
