package com.qinsong.rpc.common.serialize;

/**
 * Created by QSong
 * Contact github.com/tohodog
 * Date 2020/11/12
 */
public interface ISerialize {


    <T> byte[] serialize(T obj);

    <T> T deserialize(byte[] data, Class<T> cls);
}
