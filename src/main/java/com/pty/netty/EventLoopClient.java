package com.pty.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;

/**
 *eventloop io任务 客户端
 * @author : pety
 * @date : 2022/7/10 2:19
 */
public class EventLoopClient {
    public static void main(String[] args) throws InterruptedException {
        //带有future、promise的类型都是和异步方法配套使用，用来处理结果
       ChannelFuture future =  new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline().addLast(new StringEncoder());
                    }
                })
               //1.连接服务器
               //connect 是异步非阻塞操作，main线程发起了调用，但是真正执行connect的是另外一个nio线程
               //nio线程：NioEventLoop 中的线程
                .connect(new InetSocketAddress("localhost",8888));

       /*
       //1.调用sync的线程会阻塞，当nio建立连接后会停止阻塞
        future.sync();
        //如果没有上面sync，会不阻塞向下获取到channel1
        Channel channel = future.channel();
        //向服务器发送数据
        channel.writeAndFlush("helo");
        */

        //2.使用addListener（回调对象）方法异步处理结果
        future.addListener(new ChannelFutureListener() {
            @Override
            // 当connect方法执行完毕后，也就是连接真正建立后
            // 会在NIO线程中调用operationComplete方法
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                Channel channel = channelFuture.channel();
                channel.writeAndFlush("helo");
            }
        });
    }
}
