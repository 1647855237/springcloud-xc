package com.xuecheng.manage_cms_client.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @Author: xiongwei
 * @Date: 2019/8/28
 * @why：
 */
public interface CmsPageRepository extends MongoRepository<CmsPage,String> {

}
