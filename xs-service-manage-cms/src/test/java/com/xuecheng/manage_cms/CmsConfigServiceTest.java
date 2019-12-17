package com.xuecheng.manage_cms;

import com.xuecheng.manage_cms.service.CmsPageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


/**
 * @Author: xiongwei
 * @Date: 2019/7/31
 * @why：
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsConfigServiceTest {

    @Autowired
    private CmsPageService cmsPageService;

    @Test
    public void findPage() {
        String pageHtml = cmsPageService.getPageHtml("5d4b95ac3533c33070b4aff4");
        System.out.println();
    }


}