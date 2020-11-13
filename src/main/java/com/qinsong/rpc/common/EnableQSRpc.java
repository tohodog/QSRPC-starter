package com.qinsong.rpc.common;

import com.qinsong.rpc.client.ClientStartListener;
import com.qinsong.rpc.common.serialize.Protostuff;
import com.qinsong.rpc.server.RpcServiceLauncher;
import com.qinsong.rpc.server.ServerStartListener;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 用在SpringBoot项目的启动类上的注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({ServerStartListener.class
        , ClientStartListener.class
        , RpcServiceLauncher.class
        , Protostuff.class})
public @interface EnableQSRpc {

    boolean enabled() default true;

    /**
     * 全局qps,针对本服务所有rpc接口请求之和
     */
    float qps() default -1;

}
