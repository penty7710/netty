package com.pty.netty.chat.protocol;

/**
 * @author : pety
 * @date : 2022/7/14 19:48
 */
public interface Serializer {

    //反序列方法
    <T> T deserialize(Class<T> clazz,byte[] bytes);


    //序列化
    <T> byte []serialize(T object);
}
