package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Author: xiongwei
 * @Date: 2019/9/9
 * @why：Teachplan树形
 * */
@Mapper
public interface TeachplanMapper {

    /**
     * 查询课程树形数据
     * @param courseId
     * @return
     */
    TeachplanNode selectList(@Param("courseId") String courseId);


}
