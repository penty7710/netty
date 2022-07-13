package com.pty.netty.d3;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * 黏包半包解决方案 TCL解码器
 * @author : pety
 * @date : 2022/7/13 19:45
 */
public class TestLengthFieldDecoder {
    public static void main(String[] args) {
        EmbeddedChannel channel = new EmbeddedChannel(
                //解决方案 LengthFieldBasedFrameDecoder TCL解码器
                //第一个参数，帧的最大长度，如果超过设定值，会报错
                //第二个参数，起始偏移量，从哪里开始计算
                //第三个参数 记录长度 占用的字节，下面代码使用int型来标识长度，因此占4个字节
                //第四个参数 记录长度之后还有几个字节才到内容
                //第五个参数 解析结果的时候的偏移量，如果只想解析内容，通常是第2 ~ 第4个参数的和
                new LengthFieldBasedFrameDecoder(1024,0,4,0,4),
                new LoggingHandler(LogLevel.DEBUG)
        );

        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        send(buffer, "Hello,World");
        send(buffer,"Hi!");
        //发送数据
        channel.writeInbound(buffer);
    }

    private static void send(ByteBuf buffer, String content) {
        byte[] bytes = content.getBytes();
        int length = bytes.length;
        //先发送数据的长度
        buffer.writeInt(length);
        //在发送数据的内容
        buffer.writeBytes(bytes);
    }
}
