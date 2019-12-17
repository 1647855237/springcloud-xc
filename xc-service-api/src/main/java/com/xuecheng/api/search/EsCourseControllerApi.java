package com.xuecheng.api.search;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.QueryResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.io.IOException;
import java.util.Map;

/**
 * @Author: xiongwei
 * @Date: 2019/11/23
 * @why：
 */
@Api(value = "课程搜索",description = "课程搜索",tags = {"课程搜索"})
public interface EsCourseControllerApi {

    @ApiOperation("课程搜索")
    QueryResponseResult<CoursePub> list(int page, int size,
                                               CourseSearchParam courseSearchParam) throws IOException;
    @ApiOperation("根据课程ID搜索")
    Map<String,CoursePub> getall(String id);
}
