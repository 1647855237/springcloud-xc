package com.xuecheng.manage_cms_client.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_cms_client.dao.CmsPageRepository;
import com.xuecheng.manage_cms_client.service.PageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * @Author: xiongwei
 * @Date: 2019/8/28
 * @why：监听my消息,必须加注解，注入到spring当中，不然申明不了队列和交换机
 */
@Slf4j
@Component
public class ConsumerPostPage {
    @Autowired
    CmsPageRepository cmsPageRepository;
    @Autowired
    PageService pageService;

    /**
     *  监听队列
     * @param msg
     */
    @RabbitListener(queues = "${xuecheng.mq.queue}")
    public void postPage(String msg) {
        Map map = JSON.parseObject(msg, Map.class);
        //取出页面id
        String pageId = (String) map.get("pageId");
        log.info("consumer queue msg:{}",pageId);
        //查询页面信息
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if(!optional.isPresent()){
            log.error("receive cms post page,cmsPage is null:{}",msg);
            return ;
        }
        //将页面保存到服务器物理路径
        pageService.savePageToServerPath(pageId);
    }


}
