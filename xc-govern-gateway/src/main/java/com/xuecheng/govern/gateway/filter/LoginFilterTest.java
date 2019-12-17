package com.xuecheng.govern.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: xiongwei
 * @Date: 2019/12/11
 * @why：
 */
//@Component
public class LoginFilterTest extends ZuulFilter {

    /**
     * 过滤器类型
          pre：请求在被路由之前执行
          routing：在路由请求时调用
          post：在routing和errror过滤器之后调用
          error：处理请求时发生错误调用
     * @return
     */
    @Override
    public String filterType() {
        return "pre";
    }

    /**
     * 过滤器执行等级 越小越先执行
     * @return
     */
    @Override
    public int filterOrder() {
        return 0;
    }

    /**
     *  返回一个Boolean值，判断该过滤器是否需要执行。返回true表示要执行此过虑器
     * @return
     */
    @Override
    public boolean shouldFilter() {
        return true;
    }

    /**
     * 写逻辑的方法
     * @return
     * @throws ZuulException
     */
    @Override
    public Object run() {
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletResponse response = requestContext.getResponse();
        HttpServletRequest request = requestContext.getRequest();
        //取出头部信息Authorization
        String authorization = request.getHeader("Authorization");
        if(StringUtils.isEmpty(authorization)){
            // 拒绝访问
            requestContext.setSendZuulResponse(false);
            // 设置响应状态码
            requestContext.setResponseStatusCode(200);
            ResponseResult unauthenticated = new ResponseResult(CommonCode.UNAUTHENTICATED);
            String jsonString = JSON.toJSONString(unauthenticated);
            requestContext.setResponseBody(jsonString);
            requestContext.getResponse().setContentType("application/json;charset=UTF-8");
            return null;
        }
        return null;
    }
}
