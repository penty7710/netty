package com.pty.netty.d1;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

/**
 * netty promise
 * @author : pety
 * @date : 2022/7/12 0:16
 */
@Slf4j
public class TestNeetyPromise {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        EventLoop eventLoop = new NioEventLoopGroup().next();
        //创建一个promise
        DefaultPromise<Integer> promise = new DefaultPromise<>(eventLoop);
        new Thread(()->{
            log.debug("开始计算...");
            try {
                Thread.sleep(1000);
                int i = 1/0;
                //手动设置成功的返回值
                promise.setSuccess(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                //出现异常，设置任务失败的返回值
                promise.setFailure(e);
            }
        }).start();

        log.debug("等待接收结果");
        //同步的获取结果
        log.debug("结果是:{}",promise.get());

    }
}
