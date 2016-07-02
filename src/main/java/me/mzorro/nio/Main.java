/**
 * Created by Zorro on 6/28 028.
 */
package me.mzorro.nio;

import me.mzorro.Constant;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class Main {
    public static void ReadFromChannel(Channel channel) {

    }

    public static void ReadFromFile() throws IOException {
        String filePath = Main.class.getClassLoader().getResource(Constant.EXAMPLE_FILE_NAME).getPath();
        RandomAccessFile fin = new RandomAccessFile(filePath, "rw");
        //FileInputStream fin = new FileInputStream(filePath);
        FileChannel fc = fin.getChannel();
        ByteBuffer bb = ByteBuffer.allocate(48);
        int bytesRead = fc.read(bb);
        while (bytesRead != -1) {
            System.out.println(" Read:" + bb);
            bb.flip();
            System.out.println(" Flip:" + bb);
            System.out.println("------");
            while (bb.hasRemaining()) {
                System.out.print((char)bb.get());
            }
            System.out.println("\n------");
            System.out.println("  Get:" + bb);
            bb.clear();
            System.out.println("Clear:" + bb);
            bytesRead = fc.read(bb);
        }
        fin.close();
    }

    public static void ReadFromHttp() throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("mzorro.me", 80));
        ByteBuffer bb = ByteBuffer.wrap("GET http://mzorro.me/ HTTP/1.1\nHost: mzorro.me\n\n".getBytes());
        socketChannel.write(bb);
        bb = ByteBuffer.allocate(1024);
        int bytesRead = socketChannel.read(bb);
        while (bytesRead != -1) {
            bb.flip();
            while (bb.hasRemaining()) {
                System.out.print((char)bb.get());
            }
            bb.clear();
            bytesRead = socketChannel.read(bb);
        }
        socketChannel.close();
    }

    public static void main(String[] args) {
        try {
            //ReadFromFile();
            ReadFromHttp();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
