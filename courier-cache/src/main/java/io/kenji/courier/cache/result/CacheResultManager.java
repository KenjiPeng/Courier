package io.kenji.courier.cache.result;

import io.kenji.courier.constants.RpcConstants;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2024-01-18
 **/
public class CacheResultManager<T> {

    private final Map<CacheResultKey, T> cacheResult = new ConcurrentHashMap<>(4096);

    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final Lock readLock = lock.readLock();

    private final Lock writeLock = lock.writeLock();

    private int resultCacheExpire;

    private static volatile CacheResultManager instance;

    private CacheResultManager(int resultCacheExpire) {
        this.resultCacheExpire = resultCacheExpire;
        scanCache();
    }

    public static <T> CacheResultManager<T> getInstance(int resultCacheExpire, boolean enableResultCache) {
        if (enableResultCache && instance == null) {
            synchronized (CacheResultManager.class) {
                if (instance == null) {
                    return new CacheResultManager<>(resultCacheExpire);
                }
            }
        }
        return instance;
    }

    private void scanCache() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            if (cacheResult.size() > 0) {
                writeLock.lock();
                try {
                    Iterator<Map.Entry<CacheResultKey, T>> iterator = cacheResult.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<CacheResultKey, T> entry = iterator.next();
                        CacheResultKey key = entry.getKey();
                        if (System.currentTimeMillis() - key.getCacheTimeStamp() > resultCacheExpire) {
                            cacheResult.remove(key);
                        }
                    }
                } finally {
                    writeLock.unlock();
                }
            }
        }, 0, RpcConstants.RPC_SCAN_CACHE_TIME_INTERVAL, TimeUnit.MILLISECONDS);
    }

    public T get(CacheResultKey key) {
        return cacheResult.get(key);
    }

    public void put(CacheResultKey key, T value) {
        writeLock.lock();
        try {
            cacheResult.put(key, value);
        } finally {
            writeLock.unlock();
        }
    }
}
