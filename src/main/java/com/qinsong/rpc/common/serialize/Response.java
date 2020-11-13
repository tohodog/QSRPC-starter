package com.qinsong.rpc.common.serialize;

import java.io.Serializable;

/**
 * Created by QSong
 * Contact github.com/tohodog
 * Date 2020/11/9
 * 响应参数
 */
public class Response implements Serializable {

    private Exception exception;
    private Object result;

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

}
