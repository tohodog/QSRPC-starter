package com.qinsong.rpc;

import com.qinsong.rpc.common.EnableQSRpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableQSRpc
@SpringBootApplication(scanBasePackages = "com.qinsong")
public class RPCApplication {

    public static void main(String[] args) {
        SpringApplication.run(RPCApplication.class, args);
    }
}
