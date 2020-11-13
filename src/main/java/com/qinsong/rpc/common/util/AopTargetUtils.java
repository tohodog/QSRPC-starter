package com.qinsong.rpc.common.util;

import java.lang.reflect.Field;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.support.AopUtils;
import org.springframework.cglib.proxy.MethodInterceptor;

public class AopTargetUtils {


    /**
     * 获取 目标对象
     *
     * @param proxy 代理对象
     * @return
     * @throws Exception
     */
    public static Object getTarget(Object proxy) throws Exception {

        if (!AopUtils.isAopProxy(proxy)) {
            return proxy;//不是代理对象
        }

        if (AopUtils.isJdkDynamicProxy(proxy)) {
            return getJdkDynamicProxyTargetObject(proxy);
        } else { //cglib  
            return getCglibProxyTargetObject(proxy);
        }

    }


    private static Object getCglibProxyTargetObject(Object proxy) throws Exception {
        Field h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
        h.setAccessible(true);
        Object dynamicAdvisedInterceptor = h.get(proxy);

        Field advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");
        advised.setAccessible(true);

        Object target = ((AdvisedSupport) advised.get(dynamicAdvisedInterceptor)).getTargetSource().getTarget();

        return target;
    }

    public static Object getTrueTargetFrom1(Object obj) {
        try {
            //获取第二个拦截器
            Field field = obj.getClass().getDeclaredField("CGLIB$CALLBACK_1");
            field.setAccessible(true);
            MethodInterceptor interceptor = (MethodInterceptor) field.get(obj);

            //获取拦截器的属性advised
            Field advised = interceptor.getClass().getDeclaredField("target");
            advised.setAccessible(true);
            Object result = advised.get(interceptor);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Object getJdkDynamicProxyTargetObject(Object proxy) throws Exception {
        Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
        h.setAccessible(true);
        AopProxy aopProxy = (AopProxy) h.get(proxy);

        Field advised = aopProxy.getClass().getDeclaredField("advised");
        advised.setAccessible(true);

        Object target = ((AdvisedSupport) advised.get(aopProxy)).getTargetSource().getTarget();

        return target;
    }

}