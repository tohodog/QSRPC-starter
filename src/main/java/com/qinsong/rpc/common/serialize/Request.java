package com.qinsong.rpc.common.serialize;

import java.io.Serializable;

/**
 * Created by QSong
 * Contact github.com/tohodog
 * Date 2020/11/9
 * 请求参数
 */
public class Request implements Serializable {

    private String interfaceName;
    private String version;
    private String methodName;

    private Object[] parameters;

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }
}
