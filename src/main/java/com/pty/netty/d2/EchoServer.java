package com.pty.netty.d2;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/** 双向通信 服务端
 * @author : pety
 * @date : 2022/7/13 16:26
 */
public class EchoServer {
    public static void main(String[] args) {
         new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                //将msg转为ByteBuf对象
                                ByteBuf byteBuf = msg instanceof ByteBuf?((ByteBuf)msg):null;
                                //打印记录
                                System.out.println(byteBuf.toString(Charset.defaultCharset()));
                                //推荐使用ctx.alloc().buffer() 创建 ByteBuf对象
                                //而不是使用 ByteBufAllocator.DEFAULT.buffer(); 创建
                                ByteBuf buffer = ctx.alloc().buffer(20);
                                //将字符串转为字符存入 buffer
                                buffer.writeBytes("hello,i'm server".getBytes(StandardCharsets.UTF_8));
                                //发送
                                ctx.writeAndFlush(buffer);
                            }
                        });
                    }
                }).bind(8888);
    }
}
