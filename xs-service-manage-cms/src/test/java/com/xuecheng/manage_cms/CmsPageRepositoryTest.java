package com.xuecheng.manage_cms;

import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.framework.domain.cms.CmsPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;


/**
 * @Author: xiongwei
 * @Date: 2019/7/31
 * @why：
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsPageRepositoryTest {

    @Autowired
    private CmsPageRepository pageRepository;

    @Test
    public void findPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CmsPage> all = pageRepository.findAll(pageable);
        List<CmsPage> all1 = pageRepository.findAll();
        System.out.println(all);
    }

    /**
     * 分页带参数
     */
    @Test
    public void findPageExample() {
        // 分页对象
        Pageable pageable = PageRequest.of(0, 10);
        // 查询条件对象，给属性赋值就是要用哪个条件
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId("5a751fab6abb5044e0d19ea1");
        cmsPage.setTemplateId("5a962bf8b00ffc514038fafa");
       // cmsPage.setPageAliase("轮播");
        // 条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching();
        exampleMatcher = exampleMatcher.withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());
        // 定义Example
        Example<CmsPage> example = Example.of(cmsPage,exampleMatcher);
        Page<CmsPage> all = pageRepository.findAll(example,pageable);
        System.out.println(all);
    }

    //修改
    @Test
    public void testUpdate() {
        CmsPage byPageName = pageRepository.findByPageName("index_category.html");
        System.out.println(byPageName);
    }


}