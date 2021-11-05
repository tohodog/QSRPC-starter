package com.qinsong.rpc.test;

import com.qinsong.rpc.client.RPCFuture;
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

    @Override
    public RPCFuture<Integer> future(Integer name) {
        if (Math.random() > 0.5){ int i = 1 / 0;}
        return RPCFuture.Ok(name);
    }
}
