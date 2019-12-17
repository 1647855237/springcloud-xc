package com.xuecheng.search.service;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: xiongwei
 * @Date: 2019/11/23
 * @why：
 */
@Service
public class EsCourseService {

    @Value("${xuecheng.course.index}")
    private String es_index;
    @Value("${xuecheng.course.type}")
    private String es_type;
    @Value("${xuecheng.course.source_field}")
    private String source_field;
    @Autowired
    RestHighLevelClient restHighLevelClient;


    public QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam) {
        // 构建搜索对象
        SearchRequest searchRequest = new SearchRequest(es_index);
        searchRequest.types(es_type);
        // 构建搜索源对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //分页
        if(page<=0){
            page = 1;
        }
        if(size<=0){
            size = 20;
        }
        int start = (page-1)*size;
        searchSourceBuilder.from(start);
        searchSourceBuilder.size(size);

        String[] split = source_field.split(",");
        // 显示哪些字段
        searchSourceBuilder.fetchSource(split,new String[]{});
        // 构建bool对象
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        // 关键词对象
        if (StringUtils.isNotEmpty(courseSearchParam.getKeyword())){
            MultiMatchQueryBuilder multiMatchQueryBuilder = new MultiMatchQueryBuilder
                    (courseSearchParam.getKeyword(),"name","description","teachplan")
                    .minimumShouldMatch("70%").field("name",10);
            boolQueryBuilder.must(multiMatchQueryBuilder);
        }
        // 一级分类过滤
        if(StringUtils.isNotEmpty(courseSearchParam.getMt())){
            boolQueryBuilder.filter(QueryBuilders.termQuery("mt",courseSearchParam.getMt()));
        }
        // 二级分类过滤
        if(StringUtils.isNotEmpty(courseSearchParam.getSt())){
            boolQueryBuilder.filter(QueryBuilders.termQuery("st",courseSearchParam.getSt()));
        }
        // 难度等级过滤
        if(StringUtils.isNotEmpty(courseSearchParam.getGrade())){
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade",courseSearchParam.getGrade()));
        }

        // 请求搜索
        searchSourceBuilder.query(boolQueryBuilder);

        //高亮设置
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<font class='eslight'>");
        highlightBuilder.postTags("</font>");
        //设置高亮字段
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        searchSourceBuilder.highlighter(highlightBuilder);


        QueryResult<CoursePub> queryResult = new QueryResult<>();
        List<CoursePub> list = new ArrayList<>(16);
        try {
            searchRequest.source(searchSourceBuilder);
            // 执行搜索
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
            SearchHits searchHits = searchResponse.getHits();
            // 获取总条数
            long totalHits = searchHits.getTotalHits();
            // 获取结果
            SearchHit[] hits = searchHits.getHits();
            for (SearchHit hit : hits) {
                CoursePub coursePub = new CoursePub();
                //取出source
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                String name = (String)sourceAsMap.get("name");
                // 取出高亮字段
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                if (highlightBuilder != null) {
                    HighlightField highlightField = highlightFields.get("name");
                    if (highlightField != null) {
                        Text[] fragments = highlightField.getFragments();
                        StringBuffer stringBuffer = new StringBuffer();
                        for (Text text : fragments) {
                            stringBuffer.append(text);
                        }
                        coursePub.setName(stringBuffer.toString());
                    }
                }

                //图片
                String pic = (String) sourceAsMap.get("pic");
                coursePub.setPic(pic);
                //价格
                Double price = null;
                try {
                    if(sourceAsMap.get("price")!=null ){
                        price = (Double) sourceAsMap.get("price");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                coursePub.setPrice(price);
                Double price_old = null;
                try {
                    if(sourceAsMap.get("price_old")!=null ){
                        price_old = (Double) sourceAsMap.get("price_old");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                coursePub.setPrice_old(price_old);
                list.add(coursePub);
            }

            queryResult.setTotal(totalHits);
            queryResult.setList(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        QueryResponseResult<CoursePub> queryResponseResult = new QueryResponseResult<>(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;
    }

    public Map<String, CoursePub> getall(String id) {

        //设置索引库
        SearchRequest searchRequest = new SearchRequest(es_index);
        // 指定type
        searchRequest.types(es_type);

        // 搜索源对象
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 设置搜索条件
        sourceBuilder.query(QueryBuilders.termQuery("id",id));
        searchRequest.source(sourceBuilder);
        Map<String,CoursePub> map = new HashMap<>();
        try {
            // 执行搜索
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
            SearchHits searchHits = searchResponse.getHits();
            //总数
            long totalHits = searchHits.getTotalHits();
            SearchHit[] hits = searchHits.getHits();
            for (SearchHit hit : searchHits) {
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                String courseId = (String) sourceAsMap.get("id");
                String name = (String) sourceAsMap.get("name");
                String grade = (String) sourceAsMap.get("grade");
                String charge = (String) sourceAsMap.get("charge");
                String pic = (String) sourceAsMap.get("pic");
                String description = (String) sourceAsMap.get("description");
                String teachplan = (String) sourceAsMap.get("teachplan");
                CoursePub coursePub = new CoursePub();
                coursePub.setId(courseId);
                coursePub.setName(name);
                coursePub.setPic(pic);
                coursePub.setGrade(grade);
                coursePub.setTeachplan(teachplan);
                coursePub.setDescription(description);
                map.put(courseId,coursePub);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }
}
