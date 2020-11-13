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
    private String interfaceName;
    private int timeout;

    private ISerialize iSerialize;


    public QSRpcPorxy(Class target, QSRpcReference qsRpcReference, ISerialize iSerialize) {
        this.target = target;
        this.qsRpcReference = qsRpcReference;
        this.iSerialize = iSerialize;

        timeout = qsRpcReference.timeout();
        interfaceName = target.getName();
        if (timeout <= 0) timeout = 60 * 1000;
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
        if (qsRpcReference.value().isEmpty())
            request.setVersion(qsRpcReference.version());
        else
            request.setVersion(qsRpcReference.value());
        request.setMethodName(method.toString());
        request.setParameters(objects);


        byte[] bytes = RPCClientManager.getInstance().sendSync(request.getInterfaceName() + request.getVersion(),
                iSerialize.serialize(request), timeout);
        Response response = iSerialize.deserialize(bytes, Response.class);
        if (response.getException() != null) {
            throw response.getException();
        }
        return response.getResult();
    }


}

