package com.xuecheng.framework.exception;

import com.xuecheng.framework.model.response.ResultCode;

/**
 * @Author: xiongwei
 * @Date: 2019/8/8
 * @why： 简化抛出异常
 */
public class ExceptionCast {


    public static void cast(ResultCode commonCode){
        throw new CustomException(commonCode);
    }


}
