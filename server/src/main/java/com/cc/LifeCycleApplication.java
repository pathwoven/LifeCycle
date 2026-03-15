package com.cc;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@MapperScan("com.cc.mapper")
@SpringBootApplication
@EnableScheduling
@Slf4j
public class LifeCycleApplication {

    public static void main(String[] args) {
        log.info("LifeCycle 启动中...");
        SpringApplication.run(LifeCycleApplication.class, args);
    }

}
