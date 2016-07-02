/**
 * Created by Zorro on 7/2 002.
 */
package me.mzorro.nio.timeserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;

public class TimeServer extends SelectReactor {

    private int port;

    public TimeServer(int port) {
        this.port = port;
    }

    @Override
    protected void init() throws IOException {
        // 创建服务通道，绑定端口开始监听
        ServerSocketChannel servChannel = ServerSocketChannel.open();
        servChannel.bind(new InetSocketAddress(port));
        // 服务通道设置为非阻塞模式
        servChannel.configureBlocking(false);
        // 创建多路复用器
        selector = Selector.open();
        // 在服务通道上监听ACCEPT事件
        servChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    protected void handleInput(SelectionKey key) {
        if (key.isValid()) {
            if (key.isAcceptable()) {
                // 这是一个服务端通道，接收到了一个新的请求
                ServerSocketChannel servChannel = (ServerSocketChannel) key.channel();
                try {
                    // 调用服务通道的accept方法获得客户端通道
                    SocketChannel clientChannel = servChannel.accept();
                    // 将客户端通道设为非阻塞模式，并注册到多路复用器中，监听READ事件
                    clientChannel.configureBlocking(false);
                    clientChannel.register(selector, SelectionKey.OP_READ);
                } catch (IOException e) {
                    // 异常情况忽略
                }
            } else if (key.isReadable()) {
                // 这是一个客户端通道，现在可以读取到数据了
                SocketChannel clientChannel = (SocketChannel) key.channel();
                // 读取数据，如果数据是"QUERY TIME"，则返回当前时间
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                try {
                    int readBytes = clientChannel.read(buffer);
                    if (readBytes > 0) {
                        buffer.flip();
                        byte[] data = new byte[buffer.remaining()];
                        buffer.get(data);
                        String input = new String(data, "UTF-8");
                        String response = "QUERY TIME".equalsIgnoreCase(input) ?
                                new Date().toString() : "BAD ORDER";
                        writeToSocketChannel(clientChannel, response);
                    } else if (readBytes < 0) {
                        // 对方关闭了连接
                        key.cancel();
                        clientChannel.close();
                    }
                } catch (IOException e) {
                    // 读取客户端通道异常，认为对方关闭连接，去注册
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
        new Thread(new TimeServer(port), "TimeServer").start();
    }
}
