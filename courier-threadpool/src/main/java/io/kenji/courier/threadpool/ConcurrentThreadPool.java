package io.kenji.courier.threadpool;

import io.kenji.courier.constants.RpcConstants;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2024-01-25
 **/
public class ConcurrentThreadPool {

    private ThreadPoolExecutor threadPoolExecutor;

    private static volatile ConcurrentThreadPool instance;

    private ConcurrentThreadPool() {
    }

    private ConcurrentThreadPool(int corePoolSize, int maximumPoolSize) {
        if (corePoolSize <= 0) {
            corePoolSize = RpcConstants.DEFAULT_CORE_POOL_SIZE;
        }
        if (maximumPoolSize <= 0) {
            maximumPoolSize = RpcConstants.DEFAULT_MAXIMUM_POOL_SIZE;
        }
        if (corePoolSize > maximumPoolSize) {
            maximumPoolSize = corePoolSize;
        }
        threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, RpcConstants.DEFAULT_KEEP_ALIVE_TIME, TimeUnit.SECONDS, new ArrayBlockingQueue<>(RpcConstants.DEFAULT_QUEUE_CAPACITY));
    }


    public static ConcurrentThreadPool getInstance(int corePoolSize, int maximumPoolSize) {
        if (instance == null) {
            synchronized (ConcurrentThreadPool.class) {
                if (instance == null) {
                    instance = new ConcurrentThreadPool(corePoolSize, maximumPoolSize);
                }
            }
        }
        return instance;
    }

    public void submit(Runnable task){
        threadPoolExecutor.submit(task);
    }

    public void shutdown(){
        threadPoolExecutor.shutdown();
    }
}
