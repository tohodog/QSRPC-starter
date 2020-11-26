package com.qinsong.rpc.test;

import com.qinsong.rpc.client.QSRpcReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.*;

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

    @GetMapping("/qps")
    public String qps() {
        String result = len + "-qps:" + runTest();
        System.err.println(result);
        return result;
    }

    private long runTest() {
        requse = 0;
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        for (int i = 0; i < thread; i++) {
            EXECUTOR_SERVICE.submit(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < count; i++) {
                        irpcServer.hello("song");
                        if (requestAdd() == len) countDownLatch.countDown();
                    }
                }
            });
        }
        long t = System.currentTimeMillis();
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return len * 1000 / (System.currentTimeMillis() - t);
    }

    static volatile long requse;
    static volatile Map<Integer, Long> map = new ConcurrentHashMap<>();

    private static synchronized long requestAdd() {
        return ++requse;
    }

    private static final int DEFAULT_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;

    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(DEFAULT_THREAD_POOL_SIZE,
            DEFAULT_THREAD_POOL_SIZE * 2, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1024));

    private final static int count = 12500;//
    private final static int thread = DEFAULT_THREAD_POOL_SIZE;//x个请求线程
    private final static long len = count * thread;//总共请求
    private final static String zip = "";//gzip snappy
    private final static int timeout = 60_000;


}
