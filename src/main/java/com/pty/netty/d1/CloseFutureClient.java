package com.pty.netty.d1;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * 关闭channel并且做一些善后工作
 * @author : pety
 * @date : 2022/7/10 20:19
 */
@Slf4j
public class CloseFutureClient {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        ChannelFuture channelFuture = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new LoggingHandler((LogLevel.DEBUG)));
                        socketChannel.pipeline().addLast(new StringEncoder());
                    }
                })
                .connect(new InetSocketAddress("localhost", 8888));
        Channel channel = channelFuture.sync().channel();
        log.debug("{}",channel);

        //新建一个线程来获取输入，将输入发送给服务器
        new Thread(()->{
            Scanner sc = new Scanner(System.in);
            while(true){
                String s = sc.nextLine();
                if("q".equals(s)){
                    //关闭channel
                    channel.close();
                    break;
                }
                //将输入发送给服务器
                channel.writeAndFlush(s);
            }
        },"input").start();


        //获取CloseFuture对象
        ChannelFuture future = channel.closeFuture();
        System.out.println("获取到closefuture对象");
        //1.同步方式，sync会让主线程阻塞，直到channel调用close()方法，被关闭之后才会解开阻塞
       /* future.sync();
        log.debug("关闭后的操作");
*/
        //2. 异步方式 使用addListener,当channel被关闭后，会异步调用这个方法，执行里面的操作
        future.addListener((ChannelFutureListener) channelFuture1 -> {
            log.debug("关闭后的操作");
            //关闭eventloop，让整个程序都关闭
            group.shutdownGracefully();
        });
    }
}
