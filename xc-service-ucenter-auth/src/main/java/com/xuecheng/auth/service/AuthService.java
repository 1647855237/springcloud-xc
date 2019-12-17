package com.xuecheng.auth.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.exception.ExceptionCast;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author: xiongwei
 * @Date: 2019/12/6
 * @why：
 */
@Service
@Slf4j
public class AuthService {

    @Value("${auth.tokenValiditySeconds}")
    int tokenValiditySeconds;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    LoadBalancerClient loadBalancerClient;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    public static final String HEAR = "user_token:";


    /**
     * 认证方法
     */
    public AuthToken login(String username, String password, String clientId, String clientSecret){
        //申请令牌
        AuthToken authToken = this.applyToken(username,password,clientId, clientSecret);
        if(authToken == null){
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }
        // 令牌存放到redis中
        String access_token = authToken.getAccess_token();
        String jsonString = JSON.toJSONString(authToken);
        boolean token = this.saveToken(access_token, jsonString, this.tokenValiditySeconds);
        if (!token) {
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_TOKEN_SAVEFAIL);
        }
        return authToken;
    }

    /**
     * 从redis获取用户信息
     * @param token
     * @return
     */
    public AuthToken getUserToken(String token) {
        String userToken = HEAR+token;
        String userTokenString = stringRedisTemplate.opsForValue().get(userToken);
        if(userToken!=null){
            AuthToken authToken = null;
            try {
                authToken = JSON.parseObject(userTokenString, AuthToken.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return authToken;
        }
        return null;


    }

    /**
     * 从redis中删除令牌
     */
    public boolean delToken(String access_token){
        String name = "user_token:" + access_token;
        stringRedisTemplate.delete(name);
        return true;
    }
    /**
     *  存储令牌到redis
     * @param access_token
     * @param content
     * @param ttl
     * @return
     */
    private boolean saveToken(String access_token,String content,long ttl){
        //令牌名称
        String name = HEAR + access_token;
        //保存到令牌到redis
        stringRedisTemplate.boundValueOps(name).set(content,ttl, TimeUnit.SECONDS);
        //获取过期时间
        Long expire = stringRedisTemplate.getExpire(name);
        return expire>0;
    }

    /**
     * 申请令牌
     * @param username
     * @param password
     * @param clientId
     * @param clientSecret
     * @return
     */
    private AuthToken applyToken(String username, String password, String clientId, String clientSecret) {

        // 模拟http请求，请求spring security申请令牌
        ServiceInstance choose =
                loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        URI uri = choose.getUri();
        String authUrl = uri + "/auth/oauth/token";

        //请求的内容分两部分
        //1、header信息，包括了http basic认证信息
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        String httpbasic = httpbasic(clientId, clientSecret);
        //"Basic WGNXZWJBcHA6WGNXZWJBcHA="
        headers.add("Authorization", httpbasic);

        //2、包括：grant_type、username、passowrd
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","password");
        body.add("username",username);
        body.add("password",password);

        //URI url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType
        // url就是 申请令牌的url /oauth/token
        //method http的方法类型
        //requestEntity请求内容
        //responseType，将响应的结果生成的类型
        HttpEntity<MultiValueMap<String, String>> multiValueMapHttpEntity = new
                HttpEntity<>(body, headers);

        //指定 restTemplate当遇到400或401响应时候也不要抛出异常，也要正常返回值
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                //当响应的值为400或401时候也要正常响应，不要抛出异常
                if(response.getRawStatusCode()!=400 && response.getRawStatusCode()!=401){
                    super.handleError(response);
                }
            }
        });
        ResponseEntity<Map> exchange = null;

        try {
            //远程调用申请令牌
            exchange = restTemplate.exchange(authUrl, HttpMethod.POST,
                    multiValueMapHttpEntity, Map.class);
        } catch (RestClientException e) {
            e.printStackTrace();
            log.error(" 令牌申请错误,username and password : {}",username+password,e.getMessage());
            return null;
        }
        Map map = exchange.getBody();

        if(map == null ||
                map.get("access_token") == null ||
                map.get("refresh_token") == null ||
                map.get("jti") == null){
            //jti是jwt令牌的唯一标识作为用户身份令牌
            // 获取错误信息
            String error_description = (String) map.get("error_description");
            if(StringUtils.isNotEmpty(error_description)){
                if(error_description.equals("坏的凭证")){
                    ExceptionCast.cast(AuthCode.AUTH_CREDENTIAL_ERROR);
                }else if(error_description.indexOf("UserDetailsService returned null")>=0){
                    ExceptionCast.cast(AuthCode.AUTH_ACCOUNT_NOTEXISTS);
                }
            }
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }

        AuthToken authToken = new AuthToken();
        // 用户身份令牌
        authToken.setAccess_token((String) map.get("jti"));
        // 刷新令牌
        authToken.setRefresh_token((String) map.get("refresh_token"));
        // jwt令牌
        authToken.setJwt_token((String) map.get("access_token"));


        return authToken;
    }


    private String httpbasic(String clientId,String clientSecret){
        //将客户端id和客户端密码拼接，按“客户端id:客户端密码”
        String string = clientId+":"+clientSecret;
        //进行base64编码
        byte[] encode = Base64.encode(string.getBytes());
        return "Basic "+new String(encode);
    }


}
