package com.qinsong.rpc.client;

import com.qinsong.rpc.common.util.CGlib;
import com.qinsong.rpc.common.serialize.Request;
import com.qinsong.rpc.common.serialize.Response;
import com.qinsong.rpc.common.serialize.ISerialize;
import org.song.qsrpc.send.RPCClientManager;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * Created by QSong
 * Contact github.com/tohodog
 * Date 2020/11/13
 * 创建接口代理,实现远程调用
 */
public class QSRpcPorxy implements MethodInterceptor {

    private Class target; // 代理对象接口

    private QSRpcReference qsRpcReference;
    private String interfaceName, version;
    private int timeout;
    private String action;//选择action

    private ISerialize iSerialize;


    public QSRpcPorxy(Class target, QSRpcReference qsRpcReference, ISerialize iSerialize) {
        this.target = target;
        this.qsRpcReference = qsRpcReference;
        this.iSerialize = iSerialize;
        //初始化数据
        interfaceName = target.getName();
        if (qsRpcReference.value().isEmpty()) {
            version = qsRpcReference.version();
        } else {
            version = qsRpcReference.value();
        }
        if (qsRpcReference.ip_port().isEmpty()) {
            action = interfaceName + qsRpcReference.value();
        } else {
            action = qsRpcReference.ip_port();
        }

        timeout = qsRpcReference.timeout();
        if (timeout <= 0) timeout = RPCClientManager.RpcTimeout;
    }

    public Object getPorxy() {
        return CGlib.getPorxy(target, this);
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
//        System.out.println("调用前");
//        Object result = methodProxy.invokeSuper(o, objects); // 执行方法调用
//        System.out.println("调用后");

        Request request = new Request();
        request.setInterfaceName(interfaceName);
        request.setVersion(version);
        request.setMethodName(method.toString());
        request.setParameters(objects);

        byte[] bytes = RPCClientManager.getInstance().sendSync(action, iSerialize.serialize(request), timeout);
        Response response = iSerialize.deserialize(bytes, Response.class);
        if (response.getException() != null) {
            throw response.getException();
        }
        return response.getResult();
    }


}

