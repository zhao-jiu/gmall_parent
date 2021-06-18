package com.atguigu.gmall.test.test;

import org.aspectj.weaver.ast.Var;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author 赵赳
 * @CreateTime: 2021/4/21 11:50
 * @Description:
 */
public class CompleteTest {
   public static ThreadPoolExecutor executor = new ThreadPoolExecutor(50,
            500,
            5, TimeUnit.SECONDS,
            new  ArrayBlockingQueue<>(1000));

    public static void main(String[] args) {
        System.out.println("主线程" + Thread.currentThread().getName());

        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName());
                System.out.println("独立线程。。。");
            }
        },executor);


        CompletableFuture<Integer> completableFuture1 = CompletableFuture.supplyAsync((new Supplier<Integer>() {
            @Override
            public Integer get() {
                System.out.println(Thread.currentThread().getName());
                System.out.println("1111........");
                System.out.println("11111独立线程。。。");
                return 2;
            }
        }),executor);

        CompletableFuture<Void> completableFuture2 = completableFuture1.thenAcceptAsync(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) {
                System.out.println(Thread.currentThread().getName());
                System.out.println("22222独立线程。。。");
                System.out.println("获取到的值为：" + integer);
            }
        },executor);

        CompletableFuture.allOf(completableFuture,completableFuture1,completableFuture2).join();
    }
}
