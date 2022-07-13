package com.pty.netty.d2;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 双向通信 客户端
 * @author : pety
 * @date : 2022/7/13 16:36
 */
public class EchoClient {
    public static void main(String[] args) {
        new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                ByteBuf buffer = ctx.alloc().buffer(20);

                                buffer.writeBytes("hello".getBytes(StandardCharsets.UTF_8));
                                ctx.writeAndFlush(buffer);
                            }
                        });
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                               ByteBuf byteBuf =  msg instanceof ByteBuf?((ByteBuf)msg):null;
                                System.out.println(byteBuf.toString(Charset.defaultCharset()));
                            }
                        });
                    }
                }).connect(new InetSocketAddress("127.0.0.1",8888));
    }
}
