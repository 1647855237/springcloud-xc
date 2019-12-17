package com.xuecheng.framework.exception;

import com.xuecheng.framework.model.response.ResultCode;

/**
 * @Author: xiongwei
 * @Date: 2019/8/8
 * @why：自定义异常
 */
public class CustomException extends RuntimeException {

    ResultCode resultCode;

    public CustomException(ResultCode resultCode1){
         this.resultCode = resultCode1;
    }

    public ResultCode getCommonCode(){
        return resultCode;
    }

}
