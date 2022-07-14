package com.pty.netty.chat.client;

import com.pty.netty.chat.message.*;
import com.pty.netty.chat.protocol.MessageCodecSharable;
import com.pty.netty.chat.protocol.ProcotolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/** 聊天室客户端
 * @author : pety
 * @date : 2022/7/14 13:25
 */

@Slf4j
public class ChatClient {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        //打印 这个handler可以复用
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        //自定义协议，继承 MessageToMessageCodec 可以复用
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        //计数器
        CountDownLatch WAIT_FOR_LOGIN = new CountDownLatch(1);

        AtomicBoolean EXIT = new AtomicBoolean(false);

        AtomicBoolean login = new AtomicBoolean();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(group);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    //初始化帧解码器，避免出现黏包半包问题
                    //封装了初始化过程，让代码看起来更简洁
                    ch.pipeline().addLast(new ProcotolFrameDecoder());
                    //ch.pipeline().addLast(LOGGING_HANDLER);
                    //设置数据解码和编码
                    ch.pipeline().addLast(MESSAGE_CODEC);
                    //设置写空闲时间为3秒。如果3秒没有向服务器写数据，就会触发事件
                   ch.pipeline().addLast(new IdleStateHandler(0,3,0));

                    ch.pipeline().addLast(new ChannelDuplexHandler(){
                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                            if(evt instanceof IdleStateEvent){
                                IdleStateEvent event = (IdleStateEvent) evt;
                                if (event.state() == IdleState.WRITER_IDLE) {
                                    ctx.writeAndFlush(new PingMessage());
                                    log.info("已经3秒没有写数据了");
                                }
                            }
                        }
                    });

                    ch.pipeline().addLast("client handler",new ChannelInboundHandlerAdapter(){

                        //接收服务器发过来的数据
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            log.info("{}",msg);
                            if(msg instanceof LoginResponseMessage){
                                //将消息转为 LoginResponseMessage
                                LoginResponseMessage message = (LoginResponseMessage) msg;
                                boolean success = message.isSuccess();
                                if(success){
                                    //登陆成功，将标志位设置为true
                                    login.set(true);
                                }
                                //计数器减1
                                WAIT_FOR_LOGIN.countDown();
                            }
                        }

                        //当客户端连接上服务器后会触发 Active 事件
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            // 开辟额外线程，用于用户登陆及后续操作
                            new Thread(()->{
                                Scanner scanner = new Scanner(System.in);
                                System.out.println("请输入用户名");
                                String username = scanner.next();
                                System.out.println("请输入密码");
                                String password = scanner.next();
                                //构造发送的消息对象
                                LoginRequestMessage message = new LoginRequestMessage(username, password);
                                //发送消息
                                ctx.writeAndFlush(message);
                                System.out.println("等待后续操作...");
                                try {
                                    //阻塞方法，直到CountDownLatch 的计数器减为1
                                    WAIT_FOR_LOGIN.await();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                if(!login.get()){
                                    //登录失败，关闭channel，直接返回
                                    ctx.channel().close();
                                    return;
                                }

                                //登陆成功
                                while (true) {
                                    System.out.println("==================================");
                                    System.out.println("send [username] [content]");
                                    System.out.println("gsend [group name] [content]");
                                    System.out.println("gcreate [group name] [m1,m2,m3...]");
                                    System.out.println("gmembers [group name]");
                                    System.out.println("gjoin [group name]");
                                    System.out.println("gquit [group name]");
                                    System.out.println("quit");
                                    System.out.println("==================================");
                                    String command = scanner.nextLine();
                                    // 获得指令及其参数，并发送对应类型消息
                                    String[] commands = command.split(" ");
                                    switch (commands[0]) {
                                        case "send":
                                            ctx.writeAndFlush(new ChatRequestMessage(username, commands[1], commands[2]));
                                            break;
                                        case "gsend":
                                            ctx.writeAndFlush(new GroupChatRequestMessage(username, commands[1], commands[2]));
                                            break;
                                        case "gcreate":
                                            // 分割，获得群员名
                                            String[] members = commands[2].split(",");
                                            Set<String> set = new HashSet<>(Arrays.asList(members));
                                            // 把自己加入到群聊中
                                            set.add(username);
                                            ctx.writeAndFlush(new GroupCreateRequestMessage(commands[1], set));
                                            break;
                                        case "gmembers":
                                            ctx.writeAndFlush(new GroupMembersRequestMessage(commands[1]));
                                            break;
                                        case "gjoin":
                                            ctx.writeAndFlush(new GroupJoinRequestMessage(username, commands[1]));
                                            break;
                                        case "gquit":
                                            ctx.writeAndFlush(new GroupQuitRequestMessage(username, commands[1]));
                                            break;
                                        case "quit":
                                            ctx.channel().close();
                                            return;
                                    }
                                }
                            },"system in").start();
                        }

                        // 在连接断开时触发
                        @Override
                        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                            log.debug("连接已经断开，按任意键退出..");
                            EXIT.set(true);
                        }

                        // 在出现异常时触发
                        @Override
                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                            log.debug("连接已经断开，按任意键退出..{}", cause.getMessage());
                            EXIT.set(true);
                        }
                    });
                }
            });
            Channel channel = bootstrap.connect("localhost", 8888).sync().channel();
            channel.closeFuture().sync();
        } catch (Exception e) {
            log.error("client error", e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
