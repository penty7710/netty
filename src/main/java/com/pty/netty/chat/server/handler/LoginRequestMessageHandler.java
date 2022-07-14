package com.pty.netty.chat.server.handler;

import com.pty.netty.chat.message.LoginRequestMessage;
import com.pty.netty.chat.message.LoginResponseMessage;
import com.pty.netty.chat.server.service.UserServiceFactory;
import com.pty.netty.chat.server.session.SessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author : pety
 * @date : 2022/7/14 14:58
 */
@ChannelHandler.Sharable
public class LoginRequestMessageHandler extends SimpleChannelInboundHandler<LoginRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginRequestMessage msg) throws Exception {
        String username = msg.getUsername();
        String password = msg.getPassword();

        boolean login = UserServiceFactory.getUserService().login(username, password);
        LoginResponseMessage message;
        if (login) {
            SessionFactory.getSession().bind(ctx.channel(),username);
            message = new LoginResponseMessage(true, "登录成功");
        } else {
            message = new LoginResponseMessage(false, "登录失败");
        }
        ctx.writeAndFlush(message);
    }
}
