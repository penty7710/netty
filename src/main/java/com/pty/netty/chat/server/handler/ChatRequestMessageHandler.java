package com.pty.netty.chat.server.handler;

import com.pty.netty.chat.message.ChatRequestMessage;
import com.pty.netty.chat.message.ChatResponseMessage;
import com.pty.netty.chat.server.session.SessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author : pety
 * @date : 2022/7/14 14:59
 */
@ChannelHandler.Sharable
public class ChatRequestMessageHandler extends SimpleChannelInboundHandler<ChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatRequestMessage msg) throws Exception {
        String to = msg.getTo();

        //拿到和需要接受的用户绑定的channel
        Channel channel = SessionFactory.getSession().getChannel(to);
        //channel 不为NULL，说明用户在线，可以发送数据
        if(channel != null){
            channel.writeAndFlush(new ChatResponseMessage(msg.getFrom(),msg.getContent()));
        }else{
            channel.writeAndFlush(new ChatResponseMessage(false,"对方不在线"));
        }
    }
}
