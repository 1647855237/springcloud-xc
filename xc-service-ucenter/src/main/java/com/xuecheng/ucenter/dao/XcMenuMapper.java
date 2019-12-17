package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: xiongwei
 * @Date: 2019/12/13
 * @why：
 */
@Mapper
public interface XcMenuMapper {

    /**
     * 查找权限
     * @param userid
     * @return
     */
    List<XcMenu> selectPermissionByUserId(@Param("id") String userid);

}
