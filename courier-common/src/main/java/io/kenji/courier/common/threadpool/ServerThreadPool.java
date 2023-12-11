package io.kenji.courier.common.threadpool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-11
 **/
public class ServerThreadPool {
    private static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16, 600L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(65536));

    public static void submit(Runnable task) {
        threadPoolExecutor.submit(task);
    }

    public static void shutdown() {
        threadPoolExecutor.shutdown();
    }
}
