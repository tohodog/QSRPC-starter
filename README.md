![logo][logopng]
<br/>
<br/>
---
一个基于[QSRPC][QSRPC],结合spring-boot实现远程调用的轻量级高性能RPC框架
<br/>

[![starter][QSRPCstarter-svg]][star] [![QSRPC][QSRPCsvg]][QSRPC]  [![License][licensesvg]][license]

  * 使用nacos(2.0)/zookeeper服务发现,自动注册扩展服务
  * 使用长连接TCP池,netty作为网络IO,支持全双工通信,高性能
  * 默认使用Protostuff序列化
  * 支持压缩snappy,gzip
  * 支持针对整个/单个服务进行qps限制,超时等设置
  * 支持权重分发消息
  * 欢迎学习交流~

## Maven
```
<dependency>
    <groupId>com.github.tohodog</groupId>
    <artifactId>qsrpc-starter</artifactId>
    <version>1.0.3</version>
</dependency>

<!--导入如有问题,可尝试添加jitpack源-->
<repositories>
  <repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
  </repository>
</repositories>
``` 

## Demo(4step)
First configured 
[nacos](https://nacos.io/zh-cn/docs/deployment.html)
/
[zookeeper](http://mirrors.hust.edu.cn/apache/zookeeper/)

### 1.application.properties(yml)
```
#nacos 
qsrpc.nacos.addr=192.168.0.100:8848

#zookeeper 
#qsrpc.zk.ips=127.0.0.1:2181

#节点IP
qsrpc.node.ip=127.0.0.1 (请配置为内(外)网IP,不配置自动获取)
qsrpc.node.port=19980

#option
#请求权重(1-127) 默认平均1
#qsrpc.node.weight=1
#压缩,带宽不足可选
#qsrpc.node.zip=snappy/gzip

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
 * 消息发送支持异步(WebFlux)
 * 断路器策略
 * 服务统计治理
 * ...
 
## Test
 4-core自发自收的情况下2.3万/秒的并发数,实际会更高 [Test截图][testpng]
 
 |  CPU   | request  | time  |qps  |
 |  ----  | ----  |----  |----  |
| i3-8100(4-core/4-thread)| 10w(8-thread) | 4331ms | 23089  |
| i7-8700(6-core/12-thread) | 30w(24-thread) | 6878ms | 43617  |



## QSRPC项目技术选型及简介
### 1.TCP通信
#### 1.1 连接模式:
　本项目tcp通信使用长连接+全双工通信(两边可以同时收/发消息),可以保证更大的吞吐量/更少的连接数资源占用,理论上使用一个tcp连接即可满足通信(详见pool),如果使用http/1.1协议的请求-响应模式,同一个连接在同一个时刻只能有一个消息进行传输,如果有大量请求将会阻塞或者需要开更多tcp连接来解决
#### 1.2 协议:
|TCP|长度|消息ID|协议号|加密/压缩|内容|包尾|
|:----:|:----:|:----:|:----:|:----:|:----:|:----:|
| Byte | 4 | 4 | 1 | 1(4bit+4bit)  | n | 2 |

　首先,使用长连接那就需要解决tcp粘包问题,常见的两种方式:  
 * 包头长度:优点最简单,也是最高效的,缺点是无法感知数据包错误,会导致后续所有包错乱
 * 特定包尾:优点能感知包错误,不影响后续包,缺点需要遍历所有字节,且不能与包内容冲突
 <br/>
　综上,本框架使用的是包头长度+特定包尾,结合了两者优点,避免了缺点,高效实用,检测到包错误会自动断开.
没有使用校检码转码等,因为需要考虑实际情况,内网里出错概率非常低,出错了也能重连,对于RPC框架追求性能来说是合适的,即使是外网,后续有需求可以增加校验加密协议
<br/>
　其次,因为支持全双工那就需要解决消息回调问题,本协议使用了一个消息ID,由客户端生成,服务端返回消息带上;由于发送和接收是非连续的,所以客户端需要维护一个回调池,以ID为key,value为此次请求的context(callback),因为是异步的,请求有可能没有响应,所以池需要有超时机制

#### 1.3 压缩/加密:
　当出现带宽不足而CPU性能有余时,压缩就派上用场了,用时间换空间。目前支持了snappy/gzip两种压缩,snappy应用于google的rpc上,具有高速压缩速度和合理的压缩率,gzip速度次于snappy,但压缩率较高,根据实际情况配置,前提必须是带宽出现瓶颈/要求,否则不需要开启压缩
<br/>　加密功能计划中(加盐位算法)
#### 1.4 IO框架:
网络IO目前是基于netty搭建的,支持nio,zero-copy等特性,由于本框架连接模式使用长连接,连接数固定且较少,所以本框架性能对于IO模式(BIO/NIO/AIO)并不是很敏感,netty对于http,iot服务这种有大量连接数的优势就很大了


### 2. Tcp pool
　前面说了一个tcp连接即可支撑通信,为啥又用pool了呢,原因有两个:1. netty工作线程对于同一个连接使用同一个线程来处理的,所以如果客户端发送大量请求时,服务端只有一个线程在处理导致性能问题,起初是想服务端再把消息分发到线程池,但后续测试发现此操作在高并发下会导致延迟增大,因为又把消息放回线程池排队了。2. 相对于一条tcp链接,使用pool会更加灵活,且连接数也很少,并没有性能影响; 本框架还基于pool实现了一个[请求-响应]的通信模式*
<br>
　客户端Pool的maxIdle(maxActive)=服务节点配置的CPU线程数*2=服务节点netty的工作线程数,pool采用FIFO先行先出的策略,可以保证在高并发下均匀的使用tcp连接,服务端就不用再次分发消息了
### 3. 服务注册发现
　分布式系统中都需要一个配置/服务中心,才能进行统一管理.本框架目前使用zookeeper/nacos进行服务注册,zookeeper是使用类似文件目录的结构,每个目录都可以存一个data
<br>　节点注册是使用[IP:PROT_TIME]作为目录名,data存了节点的json数据,创建模式为EPHEMERAL_SEQUENTIAL(断开后会删除该目录),这样就达到了自动监听节点上下线的效果,加入时间戳是为了解决当节点快速重启时,注册了两个目录,便于进行区分处理
<br>　客户端通过watch目录变化信息,从而获取到所有服务节点信息,同步一个副本到本地Map里(需加上读写锁),客户端就可以实现高效调用对应的服务了


 
## Log
### v1.0.3(2021-04-16)
  * 支持Nacos 2.0
  * 支持yml,自动获取node ip
  * 其他优化...
### v1.0.2(2020-11-26)
  * 客户端支持选择调用指定节点
  * 异常处理优化
### v1.0.1(2020-11-23)
  * Upgrade dependencies
### v0.1.0(2020-11-16)
  * open source
## Other
  * 有问题请Add [issues](https://github.com/tohodog/QSRPC-starter/issues)
  * 如果项目对你有帮助的话欢迎[![star][starsvg]][star]
  
[logopng]: https://gitee.com/sakaue/QSRPC/raw/master/logo.png
[testpng]: https://gitee.com/sakaue/QSRPC-starter/raw/develop/test.png


[licensesvg]: https://img.shields.io/badge/License-Apache--2.0-red.svg
[license]: https://gitee.com/sakaue/QSRPC-starter/raw/master/LICENSE

[starsvg]: https://img.shields.io/github/stars/tohodog/QSRPC-starter.svg?style=social&label=Stars
[star]: https://github.com/tohodog/QSRPC-starter

[QSRPCsvg]: https://img.shields.io/badge/QSRPC-1.2.0-blue.svg
[QSRPC]: https://github.com/tohodog/QSRPC

[nacossvg]: https://img.shields.io/badge/nacos-2.0.0-2EBBFB.svg
[nacos]: https://github.com/alibaba/nacos

[QSRPCstarter-svg]: https://img.shields.io/badge/QSRPC%20starter-1.0.3-origen.svg
