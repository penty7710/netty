package com.pty.netty.rpc.Client.handler;

import com.pty.netty.rpc.message.RpcResponseMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ChannelHandler.Sharable
public class RpcResponseMessageHandler extends SimpleChannelInboundHandler<RpcResponseMessage> {

    //                       序号      用来接收结果的 promise 对象
    public static final Map<Integer, Promise<Object>> PROMISES = new ConcurrentHashMap<>();

    @Override

    protected void channelRead0(ChannelHandlerContext ctx, RpcResponseMessage msg) throws Exception {
        // 拿到空的 promise
        //如果存在则拿到映射并删除映射，否则返回null
        Promise<Object> promise = PROMISES.remove(msg.getSequenceId());
        if (promise != null) {
            //正常的结果
            Object returnValue = msg.getReturnValue();
            //异常的结果
            String exceptionValue = msg.getExceptionValue();
            if(exceptionValue != null) {
                promise.setFailure(new Exception(exceptionValue));
            } else {
                promise.setSuccess(returnValue);
            }
        }
    }
}
