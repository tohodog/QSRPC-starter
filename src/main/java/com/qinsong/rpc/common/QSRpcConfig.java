package com.qinsong.rpc.common;

import com.alibaba.fastjson.JSON;
import com.qinsong.rpc.common.util.IPHelp;
import org.song.qsrpc.Log;
import org.song.qsrpc.ServerConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component//加载进IOC
@ConfigurationProperties
//@PropertySource(value = {"classpath:/application-qsrpc.properties"})
public class QSRpcConfig implements InitializingBean {

    @Value("${qsrpc.cfg.log:}")
    private Boolean log;
    @Value("${qsrpc.connect.timeout:}")
    private Integer timeout;

    @Value("${qsrpc.zk.ips:}")
    private String zkIps;
    @Value("${qsrpc.zk.path:}")
    private String zkPath;
    @Value("${qsrpc.zk.username:}")
    private String zkUsername;
    @Value("${qsrpc.zk.password:}")
    private String zkPassword;

    @Value("${qsrpc.nacos.addr:}")
    private String nacosAddr;
    @Value("${qsrpc.nacos.srvname:}")
    private String nacosSrvname;

    @Value("${qsrpc.node.name:}")
    private String name;
    @Value("${qsrpc.node.ip:}")
    private String ip;
    @Value("${qsrpc.node.port:}")
    private Integer port;
    @Value("${qsrpc.node.action:}")
    private String action;
    @Value("${qsrpc.node.weight:}")
    private Byte weight;
    @Value("${qsrpc.node.zip:}")
    private String zip;
    @Value("${qsrpc.node.thread:}")
    private Integer thread;

    @Value("${qsrpc.message.maxlen:}")
    private Integer maxlen;
    @Value("${qsrpc.node.redistribute:}")
    private Boolean redistribute;

    @Value("${spring.application.name:}")
    private String defname;

    @Override
    public void afterPropertiesSet() throws Exception {
        ServerConfig.RPC_CONFIG.getNodeIp();
        if (!noNull(ip)) ip = IPHelp.getInnetIp();

        Log.i("QSRpcConfig.afterPropertiesSet:" + JSON.toJSONString(this));

        if (noNull(log)) ServerConfig.RPC_CONFIG.setPrintLog(log);
        if (noNull(timeout)) ServerConfig.RPC_CONFIG.setClientTimeout(timeout);

        if (noNull(zkIps)) ServerConfig.RPC_CONFIG.setZkIps(zkIps);
        if (noNull(zkPath)) ServerConfig.RPC_CONFIG.setZkPath(zkPath);
        if (noNull(zkUsername)) ServerConfig.RPC_CONFIG.setZkUserName(zkUsername);
        if (noNull(zkPassword)) ServerConfig.RPC_CONFIG.setZkPassword(zkPassword);

        if (noNull(nacosAddr)) ServerConfig.RPC_CONFIG.setNacosAddr(nacosAddr);
        if (noNull(nacosSrvname)) ServerConfig.RPC_CONFIG.setNacosServiceName(nacosSrvname);

        if (noNull(name)) ServerConfig.RPC_CONFIG.setNodeName(name);
        else if (noNull(defname)) ServerConfig.RPC_CONFIG.setNodeName(defname);

        if (noNull(ip)) ServerConfig.RPC_CONFIG.setNodeIp(ip);
        if (noNull(port)) ServerConfig.RPC_CONFIG.setNodePort(port);
        if (noNull(action)) ServerConfig.RPC_CONFIG.setNodeAction(action.split(","));
        if (noNull(weight)) ServerConfig.RPC_CONFIG.setNodeWeight(weight);
        if (noNull(zip)) ServerConfig.RPC_CONFIG.setNodeZip(zip);
        if (noNull(thread)) ServerConfig.RPC_CONFIG.setNodeThread(thread);

        if (noNull(maxlen)) ServerConfig.RPC_CONFIG.setNodeMaxLen(maxlen);
        if (noNull(redistribute)) ServerConfig.RPC_CONFIG.setNodeRedistribute(redistribute);

    }


    private boolean noNull(Object object) {
        if (object == null) return false;
        if (object instanceof String)
            return ((String) object).length() > 0;
        return true;
    }

    public Boolean getLog() {
        return log;
    }

    public void setLog(Boolean log) {
        this.log = log;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public String getZkIps() {
        return zkIps;
    }

    public void setZkIps(String zkIps) {
        this.zkIps = zkIps;
    }

    public String getZkPath() {
        return zkPath;
    }

    public void setZkPath(String zkPath) {
        this.zkPath = zkPath;
    }

    public String getZkUsername() {
        return zkUsername;
    }

    public void setZkUsername(String zkUsername) {
        this.zkUsername = zkUsername;
    }

    public String getZkPassword() {
        return zkPassword;
    }

    public void setZkPassword(String zkPassword) {
        this.zkPassword = zkPassword;
    }

    public String getNacosAddr() {
        return nacosAddr;
    }

    public void setNacosAddr(String nacosAddr) {
        this.nacosAddr = nacosAddr;
    }

    public String getNacosSrvname() {
        return nacosSrvname;
    }

    public void setNacosSrvname(String nacosSrvname) {
        this.nacosSrvname = nacosSrvname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Byte getWeight() {
        return weight;
    }

    public void setWeight(Byte weight) {
        this.weight = weight;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public Integer getThread() {
        return thread;
    }

    public void setThread(Integer thread) {
        this.thread = thread;
    }

    public Integer getMaxlen() {
        return maxlen;
    }

    public void setMaxlen(Integer maxlen) {
        this.maxlen = maxlen;
    }

    public Boolean getRedistribute() {
        return redistribute;
    }

    public void setRedistribute(Boolean redistribute) {
        this.redistribute = redistribute;
    }
}