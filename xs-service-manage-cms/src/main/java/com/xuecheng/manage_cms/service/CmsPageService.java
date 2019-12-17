package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.*;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @Author: xiongwei
 * @Date: 2019/8/1
 * @why：
 */
@Service
public class CmsPageService {

    @Autowired
    private CmsPageRepository cmsPageRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private GridFsTemplate gridFSTemplate;

    @Autowired
    private GridFSBucket gridFSBucket;

    @Autowired
    private CmsTemplateRepository cmsTemplateRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    /**
     * 分页带参数查询
     *
     * @param page   页码，mongodb分页查询是从0开始的，
     * @param size
     * @param queryPageRequest
     * @return
     */
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest) {
        // 条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains())
                .withMatcher("pageName",ExampleMatcher.GenericPropertyMatchers.contains());

        CmsPage cmsPage = new CmsPage();
        if (StringUtils.isNotEmpty(queryPageRequest.getSiteId())) {
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        if (StringUtils.isNotEmpty(queryPageRequest.getTemplateId())) {
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }
        if (StringUtils.isNotEmpty(queryPageRequest.getPageAliase())) {
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }
        if (StringUtils.isNotEmpty(queryPageRequest.getPageName())) {
            cmsPage.setPageName(queryPageRequest.getPageName());
        }
        if (StringUtils.isNotEmpty(queryPageRequest.getPageType())) {
            cmsPage.setPageType(queryPageRequest.getPageType());
        }
        Example<CmsPage> example = Example.of(cmsPage, exampleMatcher);
        // 分页
        if (page <= 0) {
            page = 1;
        }
        page = page - 1;
        Pageable pageable = PageRequest.of(page, size);
        Page<CmsPage> all = cmsPageRepository.findAll(example, pageable);
        QueryResult queryResult = new QueryResult();
        // 数据
        queryResult.setList(all.getContent());
        // 总数
        queryResult.setTotal(all.getTotalElements());
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS, queryResult);
        return queryResponseResult;
    }

    /**
     * 新增页面
     *
     * @param cmsPage
     * @return
     */
    public CmsPageResult add(CmsPage cmsPage) {
        // 先验证是否重复
        CmsPage cmsPage1 = cmsPageRepository.findBySiteIdAndPageTypeAndPageWebPath(cmsPage.getSiteId(), cmsPage.getPageType(), cmsPage.getPageWebPath());
        if (cmsPage1 != null) {
            // 抛出异常
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }
        cmsPage.setPageId(null);
        cmsPageRepository.save(cmsPage);
        return new CmsPageResult(CommonCode.SUCCESS, cmsPage);
    }

    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    public CmsPage getById(String id) {
        Optional<CmsPage> byId = cmsPageRepository.findById(id);
        if (byId.isPresent()) {
            return byId.get();
        }
        return null;
    }

    /**
     * 修改数据
     */
    public CmsPageResult edit(String id, CmsPage cmsPage) {
        //根据id查询页面信息
        CmsPage one = this.getById(id);
        if (one != null) {
            //更新模板id
            one.setTemplateId(cmsPage.getTemplateId());
            //更新所属站点
            one.setSiteId(cmsPage.getSiteId());
            //更新页面别名
            one.setPageAliase(cmsPage.getPageAliase());
            //更新页面名称
            one.setPageName(cmsPage.getPageName());
            //更新访问路径
            one.setPageWebPath(cmsPage.getPageWebPath());
            //更新物理路径
            one.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            one.setTemplateId(cmsPage.getTemplateId());
            cmsPageRepository.save(one);
            return new CmsPageResult(CommonCode.SUCCESS, one);
        }
        return new CmsPageResult(CommonCode.FAIL, null);
    }

    /**
     * 删除
     *
     * @param id
     * @return
     */
    public ResponseResult del(String id) {
        Optional<CmsPage> byId = cmsPageRepository.findById(id);
        if (byId.isPresent()) {
            cmsPageRepository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }




    /**
     * 页面静态化
     * 2、静态化程序获取页面的DataUrl
     * 3、静态化程序远程请求DataUrl获取数据模型。
     * 4、静态化程序获取页面的模板信息
     * 5、执行页面静态化
     *
     * @param pageId
     * @return
     */
    public String getPageHtml(String pageId) {
        // 静态化程序远程请求DataUrl获取数据模型。
        Map model = getModelByDataURl(pageId);
        // 静态化程序获取页面的模板信息
        String template = getTemplate(pageId);
        // 执行静态化
        String html = generateHtml(template, model);
        if (StringUtils.isEmpty(html)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        return html;
    }



    /**
     * 发布页面
     * @param pageId
     * @return
     */
    public ResponseResult post(String pageId) {
        // 执行静态化
        String pageHtml = this.getPageHtml(pageId);
        if(StringUtils.isEmpty(pageHtml)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        // 保存到gridFs
        CmsPage cmsPage = this.saveHtml(pageId, pageHtml);
        // 再给mq发消息
        this.sendPostPage(pageId);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 发送消息
     * @param pageId
     */
    private void sendPostPage(String pageId){
        CmsPage cmsPage = this.getById(pageId);
        // 创建消息格式
        Map<String,String> map = new HashMap<>(16);
        map.put("pageId",pageId);
        // 转成json，为了以后扩展方便，所有发送的json格式
        String jsonString = JSON.toJSONString(map);
        // 发送消息
        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE,cmsPage.getSiteId(),jsonString);
        System.out.println("发送消息"+jsonString);
    }

    // 获取信息
    private CmsPage saveHtml(String pageId,String pageHtml) {

        CmsPage cmsPage = this.getById(pageId);
        if (cmsPage == null ) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        ObjectId objectId = null;
        try {
            InputStream  inputStream = IOUtils.toInputStream(pageHtml,"utf-8");
            objectId  = gridFSTemplate.store(inputStream, cmsPage.getPageName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        cmsPage.setHtmlFileId(objectId.toString());
        return cmsPageRepository.save(cmsPage);
    }


    /**
     * 静态化处理
     *
     * @param templateName
     * @param model
     * @return
     */
    private String generateHtml(String templateName, Map model) {
        //生成配置类
        Configuration configuration = new Configuration(Configuration.getVersion());
        //模板加载器
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template", templateName);
        //配置模板加载器
        configuration.setTemplateLoader(stringTemplateLoader);
        // 获取模板
        Template template1 = null;
        try {
            template1 = configuration.getTemplate("template");
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template1, model);
            return html;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取数据
     *
     * @param pageId
     * @return
     */
    private Map getModelByDataURl(String pageId) {
        CmsPage cmsPage = this.getById(pageId);
        if (cmsPage == null) {
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        String dataUrl = cmsPage.getDataUrl();
        if (StringUtils.isEmpty(dataUrl)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        // 静态化程序远程请求DataUrl获取数据模型。
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        return forEntity.getBody();
    }

    /**
     * 获取模板
     *
     * @param pageId
     * @return
     */
    private String getTemplate(String pageId)  {
        CmsPage cmsPage = this.getById(pageId);
        if (cmsPage == null) {
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        String templateId = cmsPage.getTemplateId();
        if (StringUtils.isEmpty(templateId)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        Optional<CmsTemplate> config = cmsTemplateRepository.findById(templateId);
        if (config.isPresent()) {
            // 文件Id，根据文件id读取文件
            String templateFileId = config.get().getTemplateFileId();
            GridFSFile gridFSFile =
                    gridFSTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            //打开下载流对象
            GridFSDownloadStream gridFSDownloadStream =
                    gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            //创建gridFsResource，用于获取流对象
            GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
            //获取流中的数据
            try {
                String body = IOUtils.toString(gridFsResource.getInputStream(), "UTF-8");
                return body;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


}