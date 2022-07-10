package com.pty.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

/**
 * eventloop nio 服务端
 * @author : pety
 * @date : 2022/7/10 18:58
 */
@Slf4j
public class EventLoopServer {
    public static void main(String[] args) {
        //细分2：创建一个独立的eventloopgroup来处理
        EventLoopGroup group = new DefaultEventLoopGroup();

        new ServerBootstrap()
                //细分1：
                //第一个eventloop相当于boss，只负责ServerSocketChannel上的accpet事件
                //第二个相当于worker，负责socketchannel上的读写事件
                //如果不传参数，默认线程数是当前cpu个数的两倍
                .group(new NioEventLoopGroup(),new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline().addLast("handle1",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buf = (ByteBuf) msg;
                                log.info(buf.toString(Charset.defaultCharset()));
                                //将消息传递给下一个handle
                                ctx.fireChannelRead(msg);
                            }
                        }).addLast(group,"handle2",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buf = (ByteBuf) msg;
                                log.info(buf.toString(Charset.defaultCharset()));
                            }
                        });
                    }
                })
                .bind(8888);
    }
}
