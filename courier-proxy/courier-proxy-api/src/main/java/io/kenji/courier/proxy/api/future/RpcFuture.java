package io.kenji.courier.proxy.api.future;

import io.kenji.courier.common.threadpool.ClientThreadPool;
import io.kenji.courier.protocol.RpcProtocol;
import io.kenji.courier.protocol.enumeration.RpcStatus;
import io.kenji.courier.protocol.request.RpcRequest;
import io.kenji.courier.protocol.response.RpcResponse;
import io.kenji.courier.proxy.api.callback.AsyncRpcCallback;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author Kenji Peng
 * @Description
 * @Date 2023-12-14
 **/
@Slf4j
public class RpcFuture extends CompletableFuture<Object> {

    private final Sync sync;
    private final RpcProtocol<RpcRequest> requestRpcProtocol;
    private RpcProtocol<RpcResponse> responseRpcProtocol;
    private final long startTime;
    private long responseTimeThreshold = 5000; //Millis

    private final List<AsyncRpcCallback> pendingCallbacks = new ArrayList<>();

    private final ReentrantLock lock = new ReentrantLock();

    public RpcFuture(RpcProtocol<RpcRequest> requestRpcProtocol) {
        this.sync = new Sync();
        this.requestRpcProtocol = requestRpcProtocol;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    @Override
    public RpcResponse get() throws InterruptedException, ExecutionException {
        sync.acquire(-1);
        return Optional.ofNullable(this.responseRpcProtocol).map(RpcProtocol::getBody).orElse(null);
    }

    @Override
    public RpcResponse get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean isSuccess = sync.tryAcquireNanos(-1, unit.toNanos(timeout));
        if (isSuccess) {
            return Optional.ofNullable(this.responseRpcProtocol).map(RpcProtocol::getBody).orElse(null);
        } else {
            throw new RuntimeException("Timeout exception. Request id: " + this.requestRpcProtocol.getHeader().getRequestId() +
                    ". Request class name: " + this.requestRpcProtocol.getBody().getClassName() +
                    ". Request method: " + this.requestRpcProtocol.getBody().getMethodName());
        }
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    public void done(RpcProtocol<RpcResponse> responseRpcProtocol) {
        this.responseRpcProtocol = responseRpcProtocol;
        sync.release(1);
        invokeCallbacks();
        long responseTime = System.currentTimeMillis() - startTime;
        if (responseTime > this.responseTimeThreshold) {
            log.warn("Service response is too slow. Request id = " + responseRpcProtocol.getHeader().getRequestId() +
                    ". Response time = " + responseTime + " ms");
        }
    }


    private void runCallback(final AsyncRpcCallback asyncRpcCallback) {
        RpcResponse response = this.responseRpcProtocol.getBody();
        ClientThreadPool.submit(() -> {
            if (RpcStatus.FAIL.getCode() == this.responseRpcProtocol.getHeader().getStatus()) {
                asyncRpcCallback.onException(new RuntimeException(response.getError()));
            } else {
                asyncRpcCallback.onSuccess(response);
            }
        });
    }

    public RpcFuture addCallback(AsyncRpcCallback asyncRpcCallback) {
        lock.lock();
        try {
            if (isDone()) {
                runCallback(asyncRpcCallback);
            } else {
                pendingCallbacks.add(asyncRpcCallback);
            }
        } finally {
            lock.unlock();
        }
        return this;
    }

    private void invokeCallbacks() {
        lock.lock();
        try {
            for (final AsyncRpcCallback callback : pendingCallbacks) {
                runCallback(callback);
            }
        } finally {
            lock.unlock();
        }
    }


    private static class Sync extends AbstractQueuedSynchronizer {

        @Serial
        private static final long serialVersionUID = -7506152804203036145L;

        //future status
        private final int done = 1;
        private final int pending = 0;

        protected boolean isDone() {
            return getState() == 1;
        }

        @Override
        protected boolean tryAcquire(int acquires) {
            int state = getState();
            return state == done;
        }

        @Override
        protected boolean tryRelease(int releases) {
            if (getState() == pending) {
                return compareAndSetState(pending, done);
            }
            return false;
        }

    }
}
