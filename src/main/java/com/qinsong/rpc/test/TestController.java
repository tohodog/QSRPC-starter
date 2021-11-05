package com.qinsong.rpc.test;

import com.qinsong.rpc.client.QSRpcReference;
import com.qinsong.rpc.client.RPCFuture;
import org.song.qsrpc.send.cb.Callback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/")
public class TestController {

    @QSRpcReference
    IRPCServer irpcServer;

    @QSRpcReference(value = "2.0", ip_port = "127.0.0.1:10001")
    IRPCServer irpcServer2;

    @GetMapping("/hello")
    public Event get() {
        Event e = new Event();
        e.name = "song";
        return irpcServer.event(e);
    }

    @GetMapping("/hello2")
    public String get2() {
        return irpcServer2.hello("song");
    }

    @GetMapping("/hello3")
    public String get3() {
        RPCFuture<Integer> future = irpcServer2.future((int) (Math.random() * 10000));
        future.setCallback(new Callback<Integer>() {
            @Override
            public void handleResult(Integer result) {
                System.err.println("result:" + result);

            }

            @Override
            public void handleError(Throwable error) {
                System.err.println("error:" + error);
            }
        });
        return "ok";
    }

    @GetMapping("/qps")
    public String qps() {
        String result = len + "-同步qps:" + runTest();
        System.err.println(result);
        return result;
    }

    @GetMapping("/qps2")//异步
    public String qps2() {
        String result = len + "-异步qps:" + runTest2();
        System.err.println(result);
        return result;
    }

    private long runTest() {
        long t = System.currentTimeMillis();

        final CountDownLatch countDownLatch = new CountDownLatch(DEFAULT_THREAD_POOL_SIZE);
        for (int i = 0; i < DEFAULT_THREAD_POOL_SIZE; i++) {
            EXECUTOR_SERVICE.submit(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < count; i++) {
                        irpcServer.hello("song");
                    }
                    countDownLatch.countDown();
                }
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return len * 1000 / (System.currentTimeMillis() - t);
    }

    private static final int DEFAULT_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;

    private final static int count = 12500;//
    private final static int len = count * DEFAULT_THREAD_POOL_SIZE;//总共请求

    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(DEFAULT_THREAD_POOL_SIZE,
            DEFAULT_THREAD_POOL_SIZE * 2, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1024));


    private long runTest2() {
        long t = System.currentTimeMillis();

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final AtomicInteger atomicInteger = new AtomicInteger(0);


//        for (int i = 0; i < len; i++) {
//            RPCFuture<String> future = irpcServer.future("song");
//            future.setCallback(new Callback<String>() {
//                @Override
//                public void handleResult(String result) {
//                    int i = atomicInteger.incrementAndGet();
//                    if (i >= len) countDownLatch.countDown();
//                    if (i % 100 == 0 || i > len - 100) System.out.println("result:" + i);
//                }
//
//                @Override
//                public void handleError(Throwable error) {
//                    int i = atomicInteger.incrementAndGet();
//                    if (i >= len) countDownLatch.countDown();
//                    System.out.println("error:" + i);
//                }
//            });
//        }


        for (int i = 0; i < DEFAULT_THREAD_POOL_SIZE; i++) {
            EXECUTOR_SERVICE.submit(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < count; j++) {
                        RPCFuture<Integer> future = irpcServer.future(atomicInteger.incrementAndGet());
                        future.setCallback(new Callback<Integer>() {
                            @Override
                            public void handleResult(Integer result) {
                                int andGet = result;
                                if (andGet >= len) countDownLatch.countDown();
                                if (andGet % 100 == 0 || andGet > len - 100)
                                    System.out.println("result:" +
                                            andGet + "/" + atomicInteger.get());
                            }

                            @Override
                            public void handleError(Throwable error) {
                                int andGet = atomicInteger.get();
                                if (andGet >= len) countDownLatch.countDown();
                                System.out.println("error:" + andGet);
                            }
                        });
                    }
                }
            });
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return len * 1000 / (System.currentTimeMillis() - t);
    }
}
