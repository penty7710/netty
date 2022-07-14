package com.pty.netty.chat.server;

import com.pty.netty.chat.protocol.MessageCodecSharable;
import com.pty.netty.chat.protocol.ProcotolFrameDecoder;
import com.pty.netty.chat.server.handler.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 聊天室 服务器
 * 服务器相当于一个中转站，记录连接到服务器的用户名和对应的channel
 * 在两个客户进行聊天的时候，发送消息到服务器，服务器根据接收的用户id，将获取到的消息发送给对应的channel，完成聊天
 */

@Slf4j
public class ChatServer {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        //数据的解码和编码
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        //登录handler
        LoginRequestMessageHandler loginRequestMessageHandler = new LoginRequestMessageHandler();
        //单独聊天handler
        ChatRequestMessageHandler chatRequestMessageHandler = new ChatRequestMessageHandler();
        //创建群聊handler
        GroupCreateRequestMessageHandler groupCreateRequestMessageHandler = new GroupCreateRequestMessageHandler();
        //加入群聊handler
        GroupJoinRequestMessageHandler groupJoinRequestMessageHandler = new GroupJoinRequestMessageHandler();
        //退出群聊handler
        GroupQuitRequestMessageHandler groupQuitRequestMessageHandler = new GroupQuitRequestMessageHandler();
        //获取群聊成员handler
        GroupMembersRequestMessageHandler groupMembersRequestMessageHandler = new GroupMembersRequestMessageHandler();
        //群聊handler
        GroupChatRequestMessageHandler groupChatRequestMessageHandler = new GroupChatRequestMessageHandler();
        //退出handler
        QuitHandler quitHandler = new QuitHandler();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.group(boss, worker);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ProcotolFrameDecoder());
                    ch.pipeline().addLast(MESSAGE_CODEC);
                    //IdleStateHandler 用来判断是否是读空闲过长或者写空闲过长
                    //第一个参数是读空闲最长时间
                    //第二个是写空闲时间
                    //第三个是读写空闲时间
                    ch.pipeline().addLast(new IdleStateHandler(5,0,0));
                    //ChannelDuplexHandler 可以同时作为出站和入站处理器
                    ch.pipeline().addLast(new ChannelDuplexHandler(){
                        //用来触发特殊事件
                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                            if( evt instanceof IdleStateEvent){
                                IdleStateEvent event = (IdleStateEvent) evt;
                                // 如果触发了读空闲事件
                                if (event.state() == IdleState.READER_IDLE) {
                                    log.debug("已经5秒");
                                    //关闭连接
                                    ctx.channel().close();
                                }
                            }
                        }
                    });
                    ch.pipeline().addLast(LOGGING_HANDLER);
                    ch.pipeline().addLast(loginRequestMessageHandler);
                    ch.pipeline().addLast(groupCreateRequestMessageHandler);
                    ch.pipeline().addLast(groupJoinRequestMessageHandler);
                    ch.pipeline().addLast(groupQuitRequestMessageHandler);
                    ch.pipeline().addLast(groupMembersRequestMessageHandler);
                    ch.pipeline().addLast(groupChatRequestMessageHandler);
                    ch.pipeline().addLast(quitHandler);
                }
            });
            Channel channel = serverBootstrap.bind(8888).sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("server error", e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

}
