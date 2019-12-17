package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @Author: xiongwei
 * @Date: 2019/7/31
 * @why：mongo cmsConfig接口
 */
public interface CmsTemplateRepository extends MongoRepository<CmsTemplate,String> {

}
