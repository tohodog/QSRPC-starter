package com.qinsong.rpc.client;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by QSong
 * Contact github.com/tohodog
 * Date 2020/11/12
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface QSRpcReference {
    /**
     * 服务版本号
     */
    String value() default "";

    String version() default "";

    int timeout() default -1;//默认60s

    String ip_port() default "";//指定要请求的服务端127.0.0.1:8080

}
