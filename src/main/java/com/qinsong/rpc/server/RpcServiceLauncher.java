package com.qinsong.rpc.server;

import com.google.common.util.concurrent.RateLimiter;
import com.qinsong.rpc.common.util.CGlib;
import com.qinsong.rpc.common.EnableQSRpc;
import com.qinsong.rpc.common.serialize.Request;
import com.qinsong.rpc.common.serialize.Response;
import com.qinsong.rpc.common.serialize.ISerialize;
import com.qinsong.rpc.common.serialize.Protostuff;
import org.song.qsrpc.discover.NodeInfo;
import org.song.qsrpc.receiver.MessageListener;
import org.song.qsrpc.receiver.NodeLauncher;
import org.song.qsrpc.receiver.NodeRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Created by QSong
 * Contact github.com/tohodog
 * Date 2020/11/11
 * 启动rpc服务及消息处理
 */
@Component
public class RpcServiceLauncher {

    @Autowired(required = false)
    private ISerialize iSerialize;

    private CacheResponse cacheResponse;
    private Map<String, RpcServiceContext> contextMap;
    private EnableQSRpc enableQSRpc;
    private RateLimiter rateLimiter;


    void init(Map<String, RpcServiceContext> contextMap, EnableQSRpc enableQSRpc) {
        this.contextMap = contextMap;
        this.enableQSRpc = enableQSRpc;

        if (iSerialize == null) iSerialize = new Protostuff();

        cacheResponse = new CacheResponse(iSerialize);
        if (enableQSRpc.qps() > 0) {
            rateLimiter = RateLimiter.create(enableQSRpc.qps());
        }
    }

    private NodeLauncher.NodeContext nodeContext;

    //启动服务
    public NodeLauncher.NodeContext start() {
        stop();

        NodeInfo nodeInfo = NodeRegistry.buildNode();//read application.properties
        String[] actions = new String[contextMap.size()];
        contextMap.keySet().toArray(actions);
        nodeInfo.setActions(actions);

        nodeContext = NodeLauncher.start(nodeInfo, new MessageListener() {
            @Override
            public byte[] onMessage(Async async, byte[] message) {
                //限制qps
                if (rateLimiter != null) {
                    if (!rateLimiter.tryAcquire()) {
                        return cacheResponse.unavailable();
                    }
                }
                try {
                    return onHandle(message);
                } catch (Throwable e) {
                    e.printStackTrace();
                    //获取业务抛出的异常
                    if (e instanceof InvocationTargetException && e.getCause() != null) e = e.getCause();

                    String msg = e.getMessage();
                    if (msg == null || msg.isEmpty()) msg = e.toString();
                    Response err = new Response();
                    //统一返回Exception,防止客户端没有这个错误类,序列化失败
                    err.setException(new Exception(msg));
                    return iSerialize.serialize(err);
                }
            }
        });
        return nodeContext;
    }

    public void stop() {
        if (nodeContext != null && nodeContext.isConnect()) {
            nodeContext.close();
        }
        nodeContext = null;
    }

    //处理rpc消息
    private byte[] onHandle(byte[] message) throws InvocationTargetException {
        Request request = iSerialize.deserialize(message, Request.class);
        RpcServiceContext serviceContext = contextMap.get(request.getInterfaceName() + request.getVersion());
        if (serviceContext == null) {
            return cacheResponse.nofound();
        }

        //限制qps
        if (serviceContext.rateLimiter != null) {
            if (!serviceContext.rateLimiter.tryAcquire()) {
                return cacheResponse.unavailable();
            }
        }

        Object obj = CGlib.invoke(serviceContext.object, serviceContext.methodMap.get(request.getMethodName()), request.getParameters());
        if (obj == null) {
            return cacheResponse.empty();
        }
        Response response = new Response();
        response.setResult(obj);
        return iSerialize.serialize(response);
    }
}
