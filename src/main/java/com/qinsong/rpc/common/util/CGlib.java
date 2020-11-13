package com.qinsong.rpc.common.util;

import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by QSong
 * Contact github.com/tohodog
 * Date 2020/11/11
 */
public class CGlib {

    public static Object invoke(Object obj, Method method, Object[] parameters) throws InvocationTargetException {
        FastClass serviceFastClass = FastClass.create(obj.getClass());
        FastMethod serviceFastMethod = serviceFastClass.getMethod(method);
        return serviceFastMethod.invoke(obj, parameters);
    }

    public static Object getPorxy(Object target, Callback callback) {
        Enhancer enhancer = new Enhancer();
        // 设置父类为实例类
        if (target instanceof Class)
            enhancer.setSuperclass((Class) target);
        else
            enhancer.setSuperclass(target.getClass());

        // 回调方法
        enhancer.setCallback(callback);
        // 创建代理对象
        return enhancer.create();
    }

}
