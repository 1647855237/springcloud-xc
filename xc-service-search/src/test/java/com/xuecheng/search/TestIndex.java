package com.xuecheng.search;

import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: xiongwei
 * @Date: 2019/11/18
 * @why： 测试增删改查索引，和创建删除索引库
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestIndex {

    @Autowired
    RestHighLevelClient highLevelClient;

    @Autowired
    RestClient restClient;


    /**
     * 创建索引库
     *
     * @throws IOException
     */
    @Test
    public void testCreateIndex() throws IOException {
        // 构建删除索引对象
        CreateIndexRequest createIndexRequest = new CreateIndexRequest("xc_course");
        // 指定参数
        createIndexRequest.settings(Settings.builder().put("number_of_shards", "1").put("number_of_replicas", "0"));

        createIndexRequest.mapping("doc", "{\n" +
                "\"properties\": {\n" +
                "\t\t\t\t\"studymodel\":{\n" +
                "\t\t\t\t\t\"type\":\"keyword\"\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t\"name\":{\n" +
                "\t\t\t\t\t\"type\":\"keyword\"\n" +
                "\t\t\t\t\t},\n" +
                "\n" +
                "                    \"description\": {\n" +
                "                        \"type\": \"text\",\n" +
                "                        \"analyzer\": \"ik_max_word\",\n" +
                "                        \"search_analyzer\": \"ik_smart\"\n" +
                "                    },\n" +
                "                    \"pic\": {\n" +
                "                        \"type\": \"text\",\n" +
                "                        \"index\": false\n" +
                "                    },\n" +
                "\t                    \"timestamp\": {\n" +
                "\t\t\t\t\t\"type\": \"date\",\n" +
                "\t\t\t\t\t\"format\": \"yyyy‐MM‐dd HH:mm:ss||yyyy‐MM‐dd\"\n" +
                "\t\t\t\t\t}\n" +
                "\n" +
                "                    \n" +
                "                }\n" +
                "}", XContentType.JSON);

        // 操作索引的对象客户端
        IndicesClient indices = highLevelClient.indices();
        // 执行删除操作
        CreateIndexResponse delete = indices.create(createIndexRequest);
        // 得到响应
        boolean acknowledged = delete.isAcknowledged();
        System.out.println(acknowledged);

    }

    /**
     * 删除索引库
     *
     * @throws IOException
     */
    @Test
    public void testDeleteIndex() throws IOException {
        // 构建删除索引对象
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("xc_course");
        // 操作索引的对象客户端
        IndicesClient indices = highLevelClient.indices();
        // 执行删除操作
        DeleteIndexResponse delete = indices.delete(deleteIndexRequest);
        // 得到响应
        boolean acknowledged = delete.isAcknowledged();
        System.out.println(acknowledged);

    }


    //添加文档
    @Test
    public void testAddDoc() throws IOException {
//准备json数据
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("name", "spring cloud实战");
        jsonMap.put("description", "本课程主要从四个章节进行讲解： 1.微服务架构入门 2.spring cloud基础入门 3.实战Spring Boot 4.注册中心eureka。");
        jsonMap.put("studymodel", "201001");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy‐MM‐dd HH:mm:ss");
        jsonMap.put("timestamp", dateFormat.format(new Date()));
        jsonMap.put("price", 5.6f);
//索引请求对象
        IndexRequest indexRequest = new IndexRequest("xc_course", "doc");
//指定索引文档内容
        indexRequest.source(jsonMap);
//索引响应对象
        IndexResponse indexResponse = highLevelClient.index(indexRequest);
        //获取响应结果
        DocWriteResponse.Result result = indexResponse.getResult();
        System.out.println(result);
    }

    //查询文档
    @Test
    public void getDoc() throws IOException {
        GetRequest getRequest = new GetRequest(
                "xc_course",
                "doc",
                "4028e581617f945f01617f9dabc40000");
        GetResponse getResponse = highLevelClient.get(getRequest);
        boolean exists = getResponse.isExists();
        Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
        System.out.println(sourceAsMap);
    }

    //更新文档
    @Test
    public void updateDoc() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("xc_course", "doc",
                "4028e581617f945f01617f9dabc40000");
        Map<String, String> map = new HashMap<>();
        map.put("name", "spring cloud实战");
        updateRequest.doc(map);
        UpdateResponse update = highLevelClient.update(updateRequest);
        RestStatus status = update.status();
        System.out.println(status);
    }

    //根据id删除文档，只支持id删除
    @Test
    public void testDelDoc() throws IOException {
//删除文档id
        String id = "eqP_amQBKsGOdwJ4fHiC";
//删除索引请求对象
        DeleteRequest deleteRequest = new DeleteRequest("xc_course", "doc", id);
//响应对象
        DeleteResponse deleteResponse = highLevelClient.delete(deleteRequest);
//获取响应结果
        DocWriteResponse.Result result = deleteResponse.getResult();
        System.out.println(result);
    }


}
