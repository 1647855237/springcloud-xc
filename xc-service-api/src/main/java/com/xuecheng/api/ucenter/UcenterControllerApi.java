package com.xuecheng.api.ucenter;

import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Author: xiongwei
 * @Date: 2019/12/9
 * @why：
 */
@Api(value = "用户中心",description = "用户中心管理")
public interface UcenterControllerApi {
    @ApiOperation("根据用户名获取用户信息")
    XcUserExt getUserext(String username);

}
