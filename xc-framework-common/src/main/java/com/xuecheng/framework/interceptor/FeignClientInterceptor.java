package com.xuecheng.framework.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * @Author: xiongwei
 * @Date: 2019/12/15
 * @why： 远程调用feign拦截器，用于传递令牌，不然微服务之间无法请求
 *        这里没有注入到spring当中，那个微服务有用到，就再启动类那里注册
 */
public class FeignClientInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            // 取出请求request
            HttpServletRequest request = requestAttributes.getRequest();
            // 取出所有的header
            Enumeration<String> headerNames = request.getHeaderNames();
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    // 获取header名字，这里也可以单独传递authorization
                    String headerName = headerNames.nextElement();
                    // header的值
                    String headerValue = request.getHeader(headerName);
                    requestTemplate.header(headerName,headerValue);
                }
            }
        }
    }
}
