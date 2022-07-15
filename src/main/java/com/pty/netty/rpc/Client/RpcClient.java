package com.pty.netty.rpc.Client;


import com.pty.netty.rpc.Client.handler.RpcResponseMessageHandler;
import com.pty.netty.rpc.message.RpcRequestMessage;
import com.pty.netty.rpc.message.RpcResponseMessage;
import com.pty.netty.rpc.protocol.MessageCodecSharable;
import com.pty.netty.rpc.protocol.ProcotolFrameDecoder;
import com.pty.netty.rpc.protocol.SequenceIdGenerator;
import com.pty.netty.rpc.server.service.HelloService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;

@Slf4j
public class RpcClient {
    public static void main(String[] args) {
        HelloService service = getProxyService(HelloService.class);
        System.out.println(service.sayHello("zhangsan"));
        System.out.println(service.sayHello("李四"));
    }

    //创建代理类
    public static <T> T getProxyService(Class<T> serverClass) {
        //类加载器
        ClassLoader loader = serverClass.getClassLoader();
        Class[] interfaces = new Class[]{serverClass};
        int sequenceId = SequenceIdGenerator.nextId();
        Object o = Proxy.newProxyInstance(loader, interfaces, (proxy, method, args) -> {
            RpcRequestMessage requestMessage = new RpcRequestMessage(
                    //序列化id
                    sequenceId,
                    //类的全限定类名
                    serverClass.getName(),
                    //方法名称
                    method.getName(),
                    //方法返回值
                    method.getReturnType(),
                    //方法参数的类型
                    method.getParameterTypes(),
                    //参数
                    args
            );
            RpcClient rpcClient = new RpcClient();
            //发送消息
            rpcClient.getChannel().writeAndFlush(requestMessage);
            //准备一个空的Promise 对象来接受结果， 参数为接收结果的线程
            DefaultPromise<Object> promise = new DefaultPromise<>(rpcClient.getChannel().eventLoop());
            //将请求编号和promise放入map中，进行绑定，可以通过sequenceId 找到对应的promise
            RpcResponseMessageHandler.PROMISES.put(sequenceId,promise);

            //阻塞，直到promise有值才会停止阻塞
            promise.await();
            //如果成果直接返回
            if(promise.isSuccess()){
                return promise.getNow();
            //失败将异常抛出去
            }else{
                throw  new RuntimeException(promise.cause());
            }
        });
        return (T) o;
    }

    private static volatile Channel channel = null;

    //双重检测 ，保证channel是单例的
    public Channel getChannel() {
        if (channel == null) {
            synchronized (this) {
                if (channel == null) {
                    initChannel();
                }
            }
        }
        return channel;
    }

    //初始化channel
    private static void initChannel() {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        RpcResponseMessageHandler RPC_HANDLER = new RpcResponseMessageHandler();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(group);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new ProcotolFrameDecoder());
                ch.pipeline().addLast(LOGGING_HANDLER);
                ch.pipeline().addLast(MESSAGE_CODEC);
                ch.pipeline().addLast(RPC_HANDLER);
            }
        });
        try {
            channel = bootstrap.connect("localhost", 8080).sync().channel();
            channel.closeFuture().addListener(future -> {
                group.shutdownGracefully();
            });
        } catch (Exception e) {
            log.error("client error", e);
        }
    }
}
