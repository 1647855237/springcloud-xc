package com.xuecheng.manage_course.exception;

import com.xuecheng.framework.exception.ExcaptionCatch;
import com.xuecheng.framework.model.response.CommonCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * @Author: xiongwei
 * @Date: 2019/12/13
 * @why：课程个性异常返回格式
 */
@ControllerAdvice
public class CustomExceptionCatch extends ExcaptionCatch {

    static {
        //除了CustomException以外的异常类型及对应的错误代码在这里定义,，如果不定义则统一返回固定的错误信息
        builder.put(AccessDeniedException.class, CommonCode.UNAUTHORISE);
    }

}
