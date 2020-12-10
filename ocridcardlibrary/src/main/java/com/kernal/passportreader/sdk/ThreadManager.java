package com.kernal.passportreader.sdk;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *线程管理类
 */
public class ThreadManager {

    private volatile static ThreadPool mThreadPool;

    public static ThreadPool getThreadPool() {
        if (mThreadPool == null) {
            synchronized (ThreadManager.class) {
                if (mThreadPool == null) {
                    int cpuCount = Runtime.getRuntime().availableProcessors();// 获取cpu数量
                    int threadCount = 1;
                    mThreadPool = new ThreadPool(threadCount, threadCount, 1L);
                }
            }
        }

        return mThreadPool;
    }

    // 线程池
    public static class ThreadPool {

        private int corePoolSize;// 核心线程数
        private int maximumPoolSize;// 最大线程数
        private long keepAliveTime;// 休息时间

        private ThreadPoolExecutor executor;

        private ThreadPool(int corePoolSize, int maximumPoolSize,
                           long keepAliveTime) {
            this.corePoolSize = corePoolSize;
            this.maximumPoolSize = maximumPoolSize;
            this.keepAliveTime = keepAliveTime;
        }


        public void execute(Runnable r) {
            if (executor == null) {
                executor = new ThreadPoolExecutor(corePoolSize,
                        maximumPoolSize, keepAliveTime, TimeUnit.SECONDS,
                        new LinkedBlockingQueue<Runnable>(),
                        Executors.defaultThreadFactory(),new ThreadPoolExecutor.AbortPolicy());

            }
            executor.execute(r);
        }

        // 取消任务
        public void cancel(Runnable r) {
            if (executor != null) {
                // 从线程队列中移除对象
                executor.getQueue().remove(r);
            }
        }
    }

}
