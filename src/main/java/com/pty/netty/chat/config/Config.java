package com.pty.netty.chat.config;


import com.pty.netty.chat.protocol.Algorithm;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 配置类
 */
public abstract class Config {
    static Properties properties;
    static {
        try (InputStream in = Config.class.getResourceAsStream("/application.properties")) {
            properties = new Properties();
            properties.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    public static int getServerPort() {
        String value = properties.getProperty("server.port");
        if(value == null) {
            return 8080;
        } else {
            return Integer.parseInt(value);
        }
    }

    /**
     * 如果配置文件配置了，那么就使用配置的序列化方法，否则默认使用JAVA
     */
    public static Algorithm getSerializerAlgorithm() {
        String value = properties.getProperty("serializer.algorithm");
        if(value == null) {
            return Algorithm.JAVA;
        } else {
            return Algorithm.valueOf(value);
        }
    }
}