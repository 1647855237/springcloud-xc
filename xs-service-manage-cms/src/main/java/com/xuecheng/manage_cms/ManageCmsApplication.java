package com.xuecheng.manage_cms;

import com.xuecheng.framework.interceptor.FeignClientInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @Author: xiongwei
 * @Date: 2019/7/31
 * @why：启动类
 * EntityScan：扫描实体类
 * ComponentScan: 扫描接口
 * ComponentScan: 扫描本项目下的所有类
 * ComponentScan：扫描common项目下的所有类
 */
@SpringBootApplication
@EntityScan("com.xuecheng.framework.domain.cms")
@ComponentScan("com.xuecheng.api")
@ComponentScan("com.xuecheng.manage_cms")
@ComponentScan("com.xuecheng.framework")
public class ManageCmsApplication {
    public static void main(String[] args){
        SpringApplication.run(ManageCmsApplication.class);
    }

    /**
     * 微服务远程调用ribbon
     * @return
     */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(){
        return new RestTemplate(new OkHttp3ClientHttpRequestFactory());
    }

    /**
     * 注册feign拦截器
     * @return
     */
    @Bean
    public FeignClientInterceptor feignClientInterceptor(){
        return new FeignClientInterceptor();
    }





}
