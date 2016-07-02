/**
 * Created by Zorro on 6/28 028.
 */
package me.mzorro.nio;

import me.mzorro.Constant;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class ReadFromFile {

    public static void ReadFromFile() throws IOException {
        String filePath = ReadFromFile.class.getClassLoader().getResource(Constant.EXAMPLE_FILE_NAME).getPath();
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


    public static void main(String[] args) {
        try {
            ReadFromFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
