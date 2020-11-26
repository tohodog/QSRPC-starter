package com.qinsong.rpc.test;

import com.qinsong.rpc.server.QSRpcService;

/**
 * Created by QSong
 * Contact github.com/tohodog
 * Date 2020/11/9
 */
@QSRpcService(value = "2.0", qps = 0.5f)
public class RPCServer2 implements IRPCServer {
    @Override
    public String hello(String name) {
        return "hello(2.0):" + name;
    }

    @Override
    public Event event(Event event) {
        return event;
    }
}
