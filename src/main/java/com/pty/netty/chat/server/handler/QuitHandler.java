package com.pty.netty.chat.server.handler;

import com.pty.netty.chat.server.session.SessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author : pety
 * @date : 2022/7/14 15:49
 */
@Slf4j
@ChannelHandler.Sharable
public class QuitHandler extends ChannelInboundHandlerAdapter {

    //连接断开时会触发 Inactive 事件，
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        SessionFactory.getSession().unbind(ctx.channel());
        log.debug("{}已经断开",ctx.channel());
    }

    //捕捉住异常时会触发
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        SessionFactory.getSession().unbind(ctx.channel());
        log.debug("{}已经异常断开，异常是{}",ctx.channel(),cause);
    }
}
