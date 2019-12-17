package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @Author: xiongwei
 * @Date: 2019/7/31
 * @why：mongo cmsConfig接口
 */
public interface CmsConfigRepository extends MongoRepository<CmsConfig,String> {

}
