/**
 * Created by Zorro on 7/3 003.
 */
package me.mzorro.aio.timeserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class AsyncTimeServer implements Runnable {

    private CountDownLatch stopLatch;

    public void stop() {
        stopLatch.countDown();
    }

    private int port;
    private AsynchronousServerSocketChannel servChannel;

    public AsyncTimeServer(int port) {
        this.port = port;
        stopLatch = new CountDownLatch(1);
    }

    private static class Client {
        public AsynchronousSocketChannel clientChannel;
        public ByteBuffer buffer;
        public ReadCompletionHandler readCompletionHandler;

        public Client(AsynchronousSocketChannel clientChannel, AsyncTimeServer server) {
            this.clientChannel = clientChannel;
            this.buffer = ByteBuffer.allocate(1024);
            this.readCompletionHandler = new ReadCompletionHandler(server);
        }
    }

    private ConcurrentHashMap<AsynchronousSocketChannel, Client> clients = new ConcurrentHashMap<AsynchronousSocketChannel, Client>();

    /**
     * 创建新客户，并将其插入clients中
     * @param clientChannel
     * @return
     */
    private Client newClient(AsynchronousSocketChannel clientChannel) {
        Client client = new Client(clientChannel, this);
        clients.put(clientChannel, client);
        return client;
    }

    /**
     * 移除客户，并将其关闭
     * @param clientChannel
     */
    private void removeClient(AsynchronousSocketChannel clientChannel) {
        clients.remove(clientChannel);
        try {
            System.out.println("关闭与" + clientChannel.getRemoteAddress() + "的连接");
            clientChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class AcceptCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, AsyncTimeServer>{

        public void completed(AsynchronousSocketChannel clientChannel, AsyncTimeServer server) {
            // 获取连接完成，继续等待下一个连接
            server.servChannel.accept(server, this);
            // 创建新客户
            Client client = server.newClient(clientChannel);
            // 读取请求数据
            clientChannel.read(client.buffer, client, client.readCompletionHandler);
        }

        public void failed(Throwable exc, AsyncTimeServer server) {
            // 获取连接失败，关闭服务器
            server.stop();
        }
    }

    private static class ReadCompletionHandler implements CompletionHandler<Integer, Client> {

        AsyncTimeServer server;
        public ReadCompletionHandler(AsyncTimeServer server) {
            this.server = server;
        }

        private void writeResponse(final AsynchronousSocketChannel clientChannel, String response) {
            try {
                System.out.println("Write response: \"" + response + "\" to " + clientChannel.getRemoteAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (response != null && !response.trim().isEmpty()) {
                ByteBuffer buffer = ByteBuffer.wrap(response.getBytes());
                clientChannel.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                    public void completed(Integer result, ByteBuffer buffer) {
                        // 还没写完，继续写
                        if (buffer.hasRemaining()) {
                            clientChannel.write(buffer, buffer, this);
                        }
                    }

                    public void failed(Throwable exc, ByteBuffer buffer) {
                        // 写入失败，关闭连接
                        server.removeClient(clientChannel);
                    }
                });
            }
        }

        public void completed(Integer result, Client client) {
            if (result <= 0) return;
            // 读取成功了，从buffer中读出数据
            ByteBuffer buffer = client.buffer;
            // 先做翻转
            buffer.flip();
            // 逐字节读取，读取到'\n'表示一个命令结束
            int bytesAll = buffer.remaining();
            int i = 0;
            while (i < bytesAll && buffer.get(i) != '\n') ++i;
            if (i < bytesAll) {
                // 读取到了一个命令
                if (i != 0) {
                    byte[] bytes = new byte[i];
                    buffer.get(bytes);
                    String order = new String(bytes).trim();
                    try {
                        System.out.println("Receive order: \"" + order + "\" from " + client.clientChannel.getRemoteAddress());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String response = "query time".equalsIgnoreCase(order) ?
                            new Date().toString() : "BAD ORDER";
                    writeResponse(client.clientChannel, response);
                }
                // 这时候get应该得到'\n'了，否则出错退出
                if (buffer.get() != '\n') {
                    System.err.println("Read Error!");
                    System.exit(1);
                }
                // 处理剩下的命令
                buffer.compact();
                completed(result - (i+1), client);
            } else {
                // 没有读取到命令，恢复buffer，继续读取
                buffer.compact();
                client.clientChannel.read(buffer, client, this);
            }
        }

        public void failed(Throwable exc, Client client) {
            // 读取失败了，关闭连接
            server.removeClient(client.clientChannel);
        }
    }

    public void run() {
        try {
            servChannel = AsynchronousServerSocketChannel.open();
            servChannel.bind(new InetSocketAddress(port));
            servChannel.accept(this, new AcceptCompletionHandler());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            stopLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        int port = 8013;
        new AsyncTimeServer(port).run();
    }
}
