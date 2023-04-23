package io.kenji.courier.common.id;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023/4/23
 **/
public class IdFactory {
    private final static AtomicLong REQUEST_ID_GEN = new AtomicLong(0);
    public static Long getId(){
        return REQUEST_ID_GEN.incrementAndGet();
    }
}
