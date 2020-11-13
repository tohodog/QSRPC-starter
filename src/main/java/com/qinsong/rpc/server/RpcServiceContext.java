package com.qinsong.rpc.server;

import com.google.common.util.concurrent.RateLimiter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by QSong
 * Contact github.com/tohodog
 * Date 2020/11/11
 * 存储一个rpc接口服务的信息
 */
public class RpcServiceContext {
    public Object object;
    public Map<String, Method> methodMap = new HashMap<>();//所有方法

    public RateLimiter rateLimiter;

}
