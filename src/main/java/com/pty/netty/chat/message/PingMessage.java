package com.pty.netty.chat.message;

/**
 * @author : pety
 * @date : 2022/7/14 16:45
 */
public class PingMessage extends Message{
    @Override
    public int getMessageType() {
        return PingMessage;
    }
}
