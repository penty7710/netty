package com.pty.netty.chat.protocol;

import com.alibaba.fastjson.JSON;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * 序列化方法
 * @author : pety
 * @date : 2022/7/14 19:50
 */
public enum Algorithm implements Serializer{

    JAVA{
        @Override
        public <T> T deserialize(Class<T> clazz, byte[] bytes) {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                ObjectInputStream ois = new ObjectInputStream(bis);
                return (T)ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException("序列化失败", e);
            }
        }

        @Override
        public <T> byte[] serialize(T object) {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(object);
                return bos.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException("序列化失败", e);
            }
        }
    },
    Json{
        @Override
        public <T> T deserialize(Class<T> clazz, byte[] bytes) {
            String s = new String(bytes);
            T object = JSON.parseObject(s, clazz);
            return object;
        }

        @Override
        public <T> byte[] serialize(T object) {
            String s = JSON.toJSONString(object);
            return s.getBytes(StandardCharsets.UTF_8);
        }
    }
}
