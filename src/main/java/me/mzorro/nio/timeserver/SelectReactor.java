/**
 * Created by Zorro on 7/2 002.
 */
package me.mzorro.nio.timeserver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public abstract class SelectReactor implements Runnable {

    protected Selector selector;

    protected volatile boolean stopped;

    protected abstract void init() throws IOException;

    public void run() {
        try {
            init();
        } catch (IOException e) {
            System.out.println("Init Failed: " + e.getMessage());
            e.printStackTrace();
        }
        while (!stopped) {
            int readyChannels = 0;
            try {
                readyChannels = selector.select();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (readyChannels == 0) continue;
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();
                handleInput(key);
            }
        }
        // 关闭多路复用器，注册在上面的Channel和Pipe等资源会被自动去注册并关闭，不需要重复释放
        if (selector != null) {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected abstract void handleInput(SelectionKey key);

    protected void writeToSocketChannel(SocketChannel channel, String response) throws IOException {
        if (response != null && !response.trim().isEmpty()) {
            ByteBuffer buffer = ByteBuffer.wrap(response.getBytes());
            channel.write(buffer);
        }
    }
}
