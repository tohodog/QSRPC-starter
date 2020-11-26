package com.qinsong.rpc.test;

import com.qinsong.rpc.server.QSRpcService;

/**
 * Created by QSong
 * Contact github.com/tohodog
 * Date 2020/11/9
 */
@QSRpcService
public class RPCServer implements IRPCServer {

    @Override
    public String hello(String name) {
        return "hello:" + name;
    }

    @Override
    public Event event(Event event) {
        return event;
    }
}
