package com.qinsong.rpc.client;

import com.qinsong.rpc.common.serialize.ISerialize;
import org.song.qsrpc.send.cb.Callback;

import java.util.concurrent.*;

public class RPCFuture<T> implements Future<T>, Callback<T> {

    public static <T> RPCFuture<T> Ok(T t) {
        RPCFuture<T> future = new RPCFuture<>();
        future.handleResult(t);
        return future;
    }

    public static <T> RPCFuture<T> Error(Throwable t) {
        RPCFuture<T> future = new RPCFuture<>();
        future.handleError(t);
        return future;
    }

    private final CountDownLatch latch = new CountDownLatch(1);

    private volatile T result = null;
    private volatile Throwable error = null;

    private ISerialize iSerialize;
    private Class<T> type;

    private volatile Callback<T> callback = null;


    public RPCFuture() {
    }

    public RPCFuture(ISerialize iSerialize, Class<T> type) {
        this.iSerialize = iSerialize;
        this.type = type;
    }

    @Override
    public void handleResult(T result) {
        this.result = result;
        this.latch.countDown();
        setCallback(callback);
    }

    @Override
    public void handleError(Throwable error) {
        this.error = error;
        this.latch.countDown();
        setCallback(callback);
    }

    public synchronized void setCallback(Callback<T> callback) {
        if (isDone()) {
            if (callback == null) return;
            if (this.error != null) {
                callback.handleError(error);
            } else {
                callback.handleResult(result);
            }
        } else {
            this.callback = callback;//else 防止执行多次
        }
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        await();
        if (this.error != null) {
            throw new ExecutionException(this.error);
        } else {
            return this.result;
        }
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
        await(timeout, unit);
        if (this.error != null) {
            throw new ExecutionException(this.error);
        } else {
            return this.result;
        }
    }

    public void await() throws InterruptedException {
        this.latch.await();
    }

    public void await(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        if (!this.latch.await(timeout, unit)) {
            throw new TimeoutException("CallFuture get time out: " + unit.toMillis(timeout));
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return this.latch.getCount() <= 0L;
    }
}
