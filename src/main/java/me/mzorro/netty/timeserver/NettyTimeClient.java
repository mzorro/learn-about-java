/**
 * Created by Zorro on 7/3 003.
 */
package me.mzorro.netty.timeserver;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

public class NettyTimeClient implements Runnable {

    private int port;

    public NettyTimeClient(int port) {
        this.port = port;
    }

    public void run() {
        // 创建客户端NIO线程组
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8))
                                    .addLast(new LineBasedFrameDecoder(1024))
                                    .addLast(new StringDecoder(CharsetUtil.UTF_8))
                                    .addLast(new TimeClientHandler());
                        }
                    });
            // 连接端口，同步等待连接成功
            ChannelFuture f = b.connect("localhost", port).sync();

            // 等待客户端端口关闭
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 优雅退出，释放NIO线程组
            group.shutdownGracefully();
        }

    }

    private static class TimeClientHandler extends ChannelHandlerAdapter {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            // 通道被激活，现在可以发送请求了
            //ctx.writeAndFlush(Unpooled.copiedBuffer("query time\n".getBytes()));
            ctx.writeAndFlush("query time\n");
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            // 读取到服务端的响应数据，直接打印
            System.out.println(msg);
            ctx.close();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
        }
    }

    public static void main(String[] args) {
        new NettyTimeClient(8013).run();
    }
}
