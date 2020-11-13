package com.qinsong.rpc.server;

import com.google.common.util.concurrent.RateLimiter;
import com.qinsong.rpc.common.EnableQSRpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.song.qsrpc.receiver.NodeLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * RPC 服务器（用于发布 RPC 服务）
 */
@Service
public class ServerStartListener implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerStartListener.class);

    @Override//装载完所有bean调用,此时还没启动http服务,ApplicationRunner是启动完http端口后的
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {//root application context 没有parent，他就是老大.
            //需要执行的逻辑代码，当spring容器初始化完成后就会执行该方法。
            scan(event.getApplicationContext());
        }
    }


    @Autowired
    RpcServiceLauncher rpcServiceLauncher;
    EnableQSRpc enableQSRpc;
    Map<String, RpcServiceContext> contextMap = new HashMap<>();

    private void scan(ApplicationContext ctx) {
        Map<String, Object> startMap = ctx.getBeansWithAnnotation(EnableQSRpc.class);
        if (startMap == null || startMap.size() == 0) return;
        enableQSRpc = startMap.values().iterator().next().getClass().getAnnotation(EnableQSRpc.class);

        // 扫描带有 RpcService 注解的类
        Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(QSRpcService.class);
        if (serviceBeanMap == null || serviceBeanMap.isEmpty()) return;//没有提供rpc服务

        for (Object serviceBean : serviceBeanMap.values()) {
            QSRpcService qsRpcService = serviceBean.getClass().getAnnotation(QSRpcService.class);
            Class<?>[] interfaces = serviceBean.getClass().getInterfaces();
            if (interfaces == null || interfaces.length == 0)
                throw new IllegalArgumentException(serviceBean + " @QSRpcService 必须实现一个接口");

            for (Class i : interfaces) {
                String serviceName = i.getName();
                String serviceVersion = qsRpcService.value();
                if (serviceVersion.isEmpty()) serviceVersion = qsRpcService.version();
                if (!serviceVersion.isEmpty()) {
                    serviceName += serviceVersion;
                }
                RpcServiceContext rpcServiceContext = new RpcServiceContext();
                rpcServiceContext.object = serviceBean;
                Method[] methods = i.getMethods();
                for (Method m : methods) {
                    rpcServiceContext.methodMap.put(m.toString(), m);
                }
                float qps = qsRpcService.qps();
                if (qps > 0) rpcServiceContext.rateLimiter = RateLimiter.create(qps);

                contextMap.put(serviceName, rpcServiceContext);
            }
        }

        run();
    }

    private void run() {
        rpcServiceLauncher.init(contextMap, enableQSRpc);
        NodeLauncher.NodeContext c = rpcServiceLauncher.start();
        if (!c.isConnect()) throw new RuntimeException("QSRPC节点服务启动失败");
        LOGGER.info("QSRPC节点服务已启动^_^:" + contextMap.keySet());
    }

}