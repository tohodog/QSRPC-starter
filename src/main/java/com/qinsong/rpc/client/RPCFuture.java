package com.qinsong.rpc.client;

import com.qinsong.rpc.common.serialize.ISerialize;
import org.song.qsrpc.send.cb.Callback;

import java.util.concurrent.*;

public class RPCFuture<T> implements Future<T>, Callback<byte[]> {

    private final CountDownLatch latch = new CountDownLatch(1);

    private T result = null;
    private Throwable error = null;

    private ISerialize iSerialize;
    private Class<T> type;

    private volatile Callback<T> callback = null;

    public RPCFuture(ISerialize iSerialize, Class<T> type) {
        this.iSerialize = iSerialize;
        this.type = type;
    }


    public void handleResult(byte[] result) {
        try {
            this.result = iSerialize.deserialize(result, type);
        } catch (Exception e) {
            e.printStackTrace();
            this.error = e;
        }
        this.latch.countDown();
        setCallback(callback);
    }

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


    public T get() throws InterruptedException, ExecutionException {
        await();
        if (this.error != null) {
            throw new ExecutionException(this.error);
        } else {
            return this.result;
        }
    }

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

    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    public boolean isCancelled() {
        return false;
    }

    public boolean isDone() {
        return this.latch.getCount() <= 0L;
    }
}
