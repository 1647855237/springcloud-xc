package com.xuecheng.govern.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.govern.gateway.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: xiongwei
 * @Date: 2019/12/11
 * @why：路由过滤，只能过滤是否携带cookie，redis令牌是否过期，是否携带Authorization,但是没有验证令牌是否正确
 *      令牌正确需要再微服务认证
 */
@Component
public class LoginFilter extends ZuulFilter {

    @Autowired
    private AuthService authService;

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
        // 查询身份令牌
        String access_token = authService.getTokenFromCookie(request);
        if (access_token == null) {
            this.access_denied();
            return null;
        }
        //从redis中校验身份令牌是否过期
        long expire = authService.getExpire(access_token);
        if(expire<=0){
            access_denied();
        }
        //查询jwt令牌
        String jwt = authService.getJwtFromHeader(request);
        if(jwt == null){
            access_denied();
        }
        return null;
    }


    /**
     * 拒绝访问
     */
    private void access_denied(){
        //上下文对象
        RequestContext requestContext = RequestContext.getCurrentContext();
        //拒绝访问
        requestContext.setSendZuulResponse(false);
        //设置响应内容
        ResponseResult responseResult =new ResponseResult(CommonCode.UNAUTHENTICATED);
        String responseResultString = JSON.toJSONString(responseResult);
        requestContext.setResponseBody(responseResultString);
        //设置状态码
        requestContext.setResponseStatusCode(200);
        HttpServletResponse response = requestContext.getResponse();
        response.setContentType("application/json;charset=utf-8");
    }

}
