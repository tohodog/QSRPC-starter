package com.qinsong.rpc.server;

import com.qinsong.rpc.common.exp.NotFoundException;
import com.qinsong.rpc.common.exp.UnavailableException;
import com.qinsong.rpc.common.serialize.Response;
import com.qinsong.rpc.common.serialize.ISerialize;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by QSong
 * Contact github.com/tohodog
 * Date 2020/11/12
 * 缓存通用响应参数
 */
public class CacheResponse {

    private ISerialize iSerialize;
    private Map<String, byte[]> map = new ConcurrentHashMap<>();

    public CacheResponse(ISerialize iSerialize) {
        this.iSerialize = iSerialize;
        init();
    }

    private void init() {
        Response nofound = new Response();
        nofound.setException(new NotFoundException());
        map.put("nofound", iSerialize.serialize(nofound));

        Response unavailable = new Response();
        unavailable.setException(new UnavailableException());
        map.put("unavailable", iSerialize.serialize(unavailable));

        Response empty = new Response();
        map.put("empty", iSerialize.serialize(empty));
    }

    public byte[] nofound() {
        return map.get("nofound");
    }

    public byte[] unavailable() {
        return map.get("unavailable");
    }

    public byte[] empty() {
        return map.get("empty");
    }

}
