package test;

import core.connector.NioEndpoint;
import lombok.SneakyThrows;

public class Main {

    @SneakyThrows
    public static void main(String[] args) {
        NioEndpoint server = new NioEndpoint(100);
        server.init();
        server.start();
//        new BioEndpoint().start();
    }
}
