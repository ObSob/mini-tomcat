package scratch;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

@Log4j2
public class HttpConnector implements Runnable {
    private volatile boolean stopped = false;

    @Getter
    private String scheme = "http";

    public void stop() {
        stopped = true;
    }

    public void run() {
        ServerSocket serverSocket = null;
        int port = 8080;
        try {
            serverSocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
        } catch (Exception e) {
            log.error(e);
            System.exit(1);
        }

        while (!stopped) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
            } catch (Exception e) {
                log.error(e);
                continue;
            }
            HttpProcessor processor = new HttpProcessor(this);
            processor.process(socket);
        }
    }

    public Thread start() {
        Thread thread = new Thread(this);
        thread.start();
        return thread;
    }

}
