![logo][logopng]
<br/>
<br/>
---
一个基于[QSRPC][QSRPC],结合spring-boot实现远程调用的轻量级高性能RPC框架
<br/>

[![QSRPC][QSRPCsvg]][QSRPC]  [![License][licensesvg]][license]

  * 使用zookeeper服务发现,自动注册扩展服务
  * 使用长连接TCP池,netty作为网络IO,支持全双工通信,高性能
  * 默认使用Protostuff序列化
  * 支持压缩snappy,gzip
  * 支持针对整个/单个服务进行qps限制,超时等设置
  * 支持权重分发消息
  * 欢迎学习交流~

## Maven
```
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.gitee.sakaue</groupId>
    <artifactId>QSRPC-starter</artifactId>
    <version>1.0.0</version>
</dependency>
``` 

## Demo(4step)
First configured [zookeeper](http://mirrors.hust.edu.cn/apache/zookeeper/)

### 1.application.properties
```
#zookeeper address
qsrpc.zk.ips=127.0.0.1:2181
#rpc server address IP: WAN(8.8.8.8)/LAN(192.168.0.1)
qsrpc.node.ip=127.0.0.1
qsrpc.node.port=19980
```
### 2.SpringBootApplication
```
@EnableQSRpc//add this
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
 4-core自发自收的情况下有2.3万的并发数,实际会更高 [Test截图][testpng]
 |  CPU   | request  | time  |qps  |
 |  ----  | ----  |----  |----  |
 | i3-8100(4-core) | 10w(8-thread) | 4331ms | 23089  |

 
## Log
### v1.0.0(2020-11-16)
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
