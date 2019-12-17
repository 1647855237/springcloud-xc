package com.xuecheng.manage_cms_client.dao;

import com.xuecheng.framework.domain.cms.CmsSite;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @Author: xiongwei
 * @Date: 2019/8/28
 * @why： 站点对象
 */
public interface CmsSiteRepository extends MongoRepository<CmsSite,String> {

}
