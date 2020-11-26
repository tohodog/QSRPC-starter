package com.qinsong.rpc.test;

/**
 * Created by QSong
 * Contact github.com/tohodog
 * Date 2020/11/9
 */
public interface IRPCServer {
    String hello(String name);

    Event event(Event event);

}
