/**
 * Created by Zorro on 7/3 003.
 */
package me.mzorro.aio.timeserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

public class AsyncTimeClient implements Runnable {

    private int port;
    private CountDownLatch stopLatch;

    public AsyncTimeClient(int port) {
        this.port = port;
        stopLatch = new CountDownLatch(1);
    }

    private AsynchronousSocketChannel clientChannel;

    public void stop() {
        if (clientChannel != null) {
            try {
                clientChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        stopLatch.countDown();
    }

    public void run() {
        try {
            clientChannel = AsynchronousSocketChannel.open();
            clientChannel.connect(new InetSocketAddress("localhost", port), this, new CompletionHandler<Void, AsyncTimeClient>() {
                public void completed(Void result, AsyncTimeClient attachment) {
                    // 连接成功了，尝试发送请求
                    ByteBuffer buffer = ByteBuffer.wrap("query time\nquery time\n".getBytes());
                    clientChannel.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                        public void completed(Integer result, ByteBuffer buffer) {
                            if (buffer.hasRemaining()) {
                                // 没发完，继续发送
                                clientChannel.write(buffer, buffer, this);
                            } else {
                                // 发送完成，开始读取响应
                                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                                clientChannel.read(readBuffer, readBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                                    public void completed(Integer result, ByteBuffer buffer) {
                                        if (result <= 0) {
                                            // 没有读取到数据，关闭
                                            stop();
                                        }
                                        buffer.flip();
                                        // 读取完成，解析响应，打印出来
                                        byte[] bytes = new byte[buffer.remaining()];
                                        buffer.get(bytes);
                                        System.out.println(new String(bytes));
                                        // 清掉缓存继续读
                                        buffer.clear();
                                        clientChannel.read(buffer, buffer, this);
                                    }

                                    public void failed(Throwable exc, ByteBuffer buffer) {
                                        // 读取失败，关闭
                                        stop();
                                    }
                                });
                            }
                        }

                        public void failed(Throwable exc, ByteBuffer buffer) {
                            // 发送请求失败，关闭
                            stop();
                        }
                    });
                }

                public void failed(Throwable exc, AsyncTimeClient attachment) {
                    // 连接服务器失败，关闭
                    stop();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            stopLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port = 8013;
        new AsyncTimeClient(port).run();
    }
}
