package test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;

public class Nio2EndpointTest {

    public static void main(String[] args) {
        try {
            // 创建并绑定 AsynchronousServerSocketChannel
            final AsynchronousServerSocketChannel serverChannel = AsynchronousServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress("localhost", 9001));
            System.out.println("Server listening on port 8080");

            // 接收连接请求
            serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
                @Override
                public void completed(AsynchronousSocketChannel clientChannel, Void attachment) {
                    // 继续接受其他连接请求
                    serverChannel.accept(null, this);

                    // 处理客户端连接
                    handleClient(clientChannel);
                }

                @Override
                public void failed(Throwable exc, Void attachment) {
                    System.err.println("Failed to accept connection: " + exc);
                }
            });

            // 阻止主线程退出
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(AsynchronousSocketChannel clientChannel) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        // 异步读取数据
        clientChannel.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                attachment.flip();
                String message = StandardCharsets.UTF_8.decode(attachment).toString();
                System.out.println("Received message: " + message);

                // 响应客户端
                String response = "Echo: " + message;
                ByteBuffer responseBuffer = ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8));
                clientChannel.write(responseBuffer, responseBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                    @Override
                    public void completed(Integer result, ByteBuffer attachment) {
                        if (attachment.hasRemaining()) {
                            clientChannel.write(attachment, attachment, this);
                        } else {
                            // 继续读取数据
                            buffer.clear();
                            clientChannel.read(buffer, buffer, this);
                        }
                    }

                    @Override
                    public void failed(Throwable exc, ByteBuffer attachment) {
                        System.err.println("Failed to write response: " + exc);
                        try {
                            clientChannel.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                System.err.println("Failed to read message: " + exc);
                try {
                    clientChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
