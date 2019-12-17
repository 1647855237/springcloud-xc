package com.xuecheng.api.course;

import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.TeachplanMedia;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Author: xiongwei
 * @Date: 2019/9/9
 * @why：课程管理接口
 */
@Api(value="课程管理接口",description = "课程管理接口，提供课程的管理、查询接口")
public interface CourseControllerApi {

    @ApiOperation("课程计划查询")
    TeachplanNode findTeachplanList(String courseId);

    @ApiOperation("添加课程计划")
    ResponseResult addTeachplan(Teachplan teachplan);

    @ApiOperation("我的课程查询，变成下面那个了，这个未作")
    ResponseResult listTeachplan(Teachplan teachplan);

    @ApiOperation("保存媒资信息")
    ResponseResult savemedia(TeachplanMedia teachplanMedia);

    @ApiOperation("我的课程查询")
    QueryResponseResult<CourseInfo> findCourseList(int page,
                                                          int size,
                                                          CourseListRequest courseListRequest);



}
