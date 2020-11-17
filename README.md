![logo][logopng]
<br/>
<br/>
---
一个基于[QSRPC][QSRPC],结合spring-boot实现远程调用的轻量级高性能RPC框架
<br/>

[![star][QSRPCstarter-svg]][star] [![QSRPC][QSRPCsvg]][QSRPC]  [![License][licensesvg]][license]

  * 使用zookeeper服务发现,自动注册扩展服务
  * 使用长连接TCP池,netty作为网络IO,支持全双工通信,高性能
  * 默认使用Protostuff序列化
  * 支持压缩snappy,gzip
  * 支持针对整个/单个服务进行qps限制,超时等设置
  * 支持权重分发消息
  * 欢迎学习交流~

## Maven
```
<dependencies>
    <dependency>
        <groupId>com.gitee.sakaue</groupId>
        <artifactId>QSRPC-starter</artifactId>
        <version>0.1.0</version>
    </dependency>
<dependencies>

<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
``` 

## Demo(4step)
First configured [zookeeper](http://mirrors.hust.edu.cn/apache/zookeeper/)

### 1.application.properties
```
#zookeeper addresses
qsrpc.zk.ips=127.0.0.1:2181
#rpc server address
qsrpc.node.ip=127.0.0.1 (请配置为内(外)网IP)
qsrpc.node.port=19980

#option
#请求权重 默认平均1
qsrpc.node.weight=1
#压缩,带宽不足可选
qsrpc.node.zip=snappy/gzip

```
### 2.SpringBootApplication
```
@EnableQSRpc//add this
//@EnableQSRpc(qps = 100000) 限制整个服务qps
@SpringBootApplication
public class RPCApplication {
    public static void main(String[] args) {
        SpringApplication.run(RPCApplication.class, args);
    }
}
```

### 3.public api
```
public interface IRPCServer {
    String hello(String name);
}
```
### 4.1 server
```
@QSRpcService
//@QSRpcService(value = "2.0", qps = 1f) 设置版本号及该服务qps
public class RPCServer implements IRPCServer {
    @Override
    public String hello(String name) {
        return "hello:" + name;
    }
}
```
### 4.2 client
```
@QSRpcReference
//@QSRpcReference(version = "2.0",timeout = 10000) 配置版本号及超时
IRPCServer rpcServer;

@ResponseBody
@GetMapping("/hello")
public String hello() {
    return rpcServer.hello("QSPRC");
}
```

## Future
 * 消息发送支持异步
 * 服务统计
 * 断路器策略
 * ...
 
## Test
 4-core自发自收的情况下2.3万/秒的并发数,实际会更高 [Test截图][testpng]
 
 |  CPU   | request  | time  |qps  |
 |  ----  | ----  |----  |----  |
| i3-8100(4-core/4-thread)| 10w(8-thread) | 4331ms | 23089  |
| i7-8700(6-core/12-thread) | 30w(24-thread) | 6878ms | 43617  |

 
## Log
### v0.1.0(2020-11-16)
  * open source
## Other
  * 有问题请Add [issues](https://gitee.com/sakaue/QSRPC-starter/issues)
  * 如果项目对你有帮助的话欢迎[star][star]
  
[logopng]: https://gitee.com/sakaue/QSRPC/raw/master/logo.png
[testpng]: https://gitee.com/sakaue/QSRPC-starter/raw/develop/test.png


[licensesvg]: https://img.shields.io/badge/License-Apache--2.0-red.svg
[license]: https://gitee.com/sakaue/QSRPC-starter/raw/master/LICENSE

[starsvg]: https://img.shields.io/github/stars/tohodog/QSRPC-starter.svg?style=social&label=Stars
[star]: https://gitee.com/sakaue/QSRPC-starter

[QSRPCsvg]: https://img.shields.io/badge/QSRPC-1.1.0-blue.svg
[QSRPC]: https://gitee.com/sakaue/QSRPC

[QSRPCstarter-svg]: https://img.shields.io/badge/QSRPC%20starter-0.1.0-origen.svg
