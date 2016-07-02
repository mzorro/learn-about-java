/**
 * Created by Zorro on 7/2 002.
 */
package me.mzorro.nio.timeserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class TimerClient extends SelectReactor {

    private int port;

    public TimerClient(int port) {
        this.port = port;
    }

    @Override
    protected void init() throws IOException {
        // 打开一个客户端通道
        SocketChannel clientChannel = SocketChannel.open();
        // 设置客户端通道为非阻塞的
        clientChannel.configureBlocking(false);
        // 打开多路复用器
        selector = Selector.open();
        // 尝试连接服务器端口
        // 异步连接，如果返回false则说明客户端已经发送sync包，服务端没有返回ack包
        boolean connected = clientChannel.connect(new InetSocketAddress("localhost", port));
        if (connected) {
            // 连接成功，监听READ事件，并发送请求
            handelConnected(clientChannel);
        } else {
            // 连接尚未成功，监听CONNECT事件
            clientChannel.register(selector, SelectionKey.OP_CONNECT);
        }
    }

    /**
     * 连接成功，监听READ事件，并发送请求
     *
     * @param clientChannel 客户端通道
     */
    private void handelConnected(SocketChannel clientChannel) throws IOException {
        clientChannel.register(selector, SelectionKey.OP_READ);
        writeToSocketChannel(clientChannel, "query time");
    }

    protected void handleInput(SelectionKey key) {
        if (key.isValid()) {
            if (key.isConnectable()) {
                // 收到连接事件
                SocketChannel clientChannel = (SocketChannel) key.channel();
                try {
                    if (clientChannel.finishConnect()) {
                        // 连接成功，监听READ事件，并发送请求
                        handelConnected(clientChannel);
                    } else throw new IOException();
                } catch (IOException e) {
                    // 连接不成功，退出
                    System.out.println("Connection Failed: " + e.getMessage());
                    System.exit(1);
                }
            } else if (key.isReadable()) {
                // 现在可以读取服务器端的响应
                SocketChannel clientChannel = (SocketChannel) key.channel();
                // 读取数据
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                try {
                    int readBytes = clientChannel.read(buffer);
                    if (readBytes > 0) {
                        buffer.flip();
                        byte[] data = new byte[buffer.remaining()];
                        buffer.get(data);
                        String input = new String(data, "UTF-8");
                        System.out.println("Now is " + input);
                        this.stopped = true;
                    } else if (readBytes < 0) {
                        // 对方关闭了连接
                        key.cancel();
                        clientChannel.close();
                    }
                } catch (IOException e) {
                    // 读取服务器异常，认为对方关闭连接，去注册
                    key.cancel();
                    try {
                        clientChannel.close();
                    } catch (IOException e1) {
                        // 忽略
                    }
                }

            }
        }
    }

    public static void main(String[] args) {
        int port = 8013;
        new Thread(new TimerClient(port), "TimerClient").start();
    }
}
