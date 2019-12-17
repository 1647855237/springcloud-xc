package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.Teachplan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by Administrator.
 */
public interface TeachplanRepository extends JpaRepository<Teachplan,String> {

    /**
     * 查询是否有根节点
     * @param courseId
     * @param parentId
     * @return
     */
    List<Teachplan> findByCourseidAndParentid(String courseId,String parentId);

}
