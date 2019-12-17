package com.xuecheng.manage_course;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Administrator
 * @version 1.0
 **/
@EnableFeignClients //开始feignClient
@EnableDiscoveryClient
@SpringBootApplication
@EntityScan("com.xuecheng.framework.domain.course")//扫描实体类
@ComponentScan("com.xuecheng.api")
@ComponentScan("com.xuecheng.manage_course")
@ComponentScan("com.xuecheng.framework")
public class ManageCourseApplication {
    public static void main(String[] args){
        SpringApplication.run(ManageCourseApplication.class, args);
    }
}
