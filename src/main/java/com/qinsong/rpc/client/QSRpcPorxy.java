package com.qinsong.rpc.client;

import com.qinsong.rpc.common.serialize.ISerialize;
import com.qinsong.rpc.common.serialize.Request;
import com.qinsong.rpc.common.serialize.Response;
import com.qinsong.rpc.common.util.CGlib;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.song.qsrpc.send.RPCClientManager;
import org.song.qsrpc.send.cb.Callback;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(QSRpcPorxy.class);

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
            action = interfaceName + version;
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

        Class<?> returnType = method.getReturnType();
        if (RPCFuture.class.isAssignableFrom(returnType)) {
//            Type type = returnType.getGenericSuperclass();
//            if (!(type instanceof ParameterizedType)) {
//                type = String.class;
//            } else {
//                ParameterizedType parameterizedType = (ParameterizedType) type;
//                type = parameterizedType.getRawType();
//            }
            //异步请求
            final RPCFuture<Object> rpcFuture = new RPCFuture<>();

            RPCClientManager.getInstance().sendAsync(action, iSerialize.serialize(request), new Callback<byte[]>() {
                @Override
                public void handleResult(byte[] result) {
                    Response response = iSerialize.deserialize(result, Response.class);
                    if (response.getException() != null) {
                        LOGGER.error("RPCFuture.handleError", response.getException());
                        rpcFuture.handleError(response.getException());
                    } else {
                        rpcFuture.handleResult(response.getResult());
                    }
                }

                @Override
                public void handleError(Throwable error) {
                    error.printStackTrace();
                    rpcFuture.handleError(error);
                }
            }, timeout);

            return rpcFuture;
        } else {
            //同步请求
            byte[] bytes = RPCClientManager.getInstance().sendSync(action, iSerialize.serialize(request), timeout);
            Response response = iSerialize.deserialize(bytes, Response.class);
            if (response.getException() != null) {
//                response.getException().printStackTrace();
                LOGGER.error("Response.exception", response.getException());
                throw response.getException();
            }
            return response.getResult();
        }
    }


}

