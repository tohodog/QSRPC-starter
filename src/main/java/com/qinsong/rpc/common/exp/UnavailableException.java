package com.qinsong.rpc.common.exp;

/**
 * Created by QSong
 * Contact github.com/tohodog
 * Date 2020/11/25
 * 拒绝处理
 */
public class UnavailableException extends Exception {
    public UnavailableException() {
        super("UnavailableException");
    }
}
