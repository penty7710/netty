package com.pty.netty;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * jdk future
 * @author : pety
 * @date : 2022/7/11 23:42
 */
@Slf4j
public class TestJdkFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //1.创建线程池
        ExecutorService service = Executors.newFixedThreadPool(2);
        //2.提交任务
        Future<Integer> future = service.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                log.debug("执行计算");
                Thread.sleep(1000);
                return 50;
            }
        });

        log.debug("等待结果");
        //get()方法 同步阻塞，会一直等到任务执行完毕，然后拿到任务的返回值
        log.debug("结果是：{}",future.get());
    }
}
