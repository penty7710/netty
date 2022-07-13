package com.pty.netty.d1;

import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * @author : pety
 * @date : 2022/7/11 23:53
 */
@Slf4j
public class TestNettyFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        EventLoop eventLoop = group.next();
        Future<Integer> future = eventLoop.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                log.debug("执行计算");
                Thread.sleep(1000);
                return 100;
            }
        });

        //1.get()方法，同步的方式
        /*log.debug("等待结果");
        log.debug("结果是:{}",future.get());*/

        //2.addListener 异步执行
        future.addListener(new GenericFutureListener<Future<? super Integer>>() {
            @Override
            public void operationComplete(Future<? super Integer> future) throws Exception {
                log.debug("等待结果");
                //getNow 非阻塞方法，因为回调函数被调用了，说明之前那个任务已经执行完了，那么也一定有结果了
                //getNow() 如果没有结果会返回null，不会阻塞
                log.debug("结果是:{}",future.getNow());
            }
        });
    }
}
