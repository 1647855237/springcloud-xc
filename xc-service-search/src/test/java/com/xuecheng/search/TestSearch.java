package com.xuecheng.search;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Map;

/**
 * @Author: xiongwei
 * @Date: 2019/11/18
 * @why：测试DSL搜索
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestSearch {

    @Autowired
    RestHighLevelClient client;

    @Autowired
    RestClient restClient;

    /**
     * 搜索全部
     *
     * @throws IOException
     */
    @Test
    public void testSearch() throws IOException {
        // 构建搜索对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        // 指定类型
        searchRequest.types("doc");
        // 构建搜索源对象
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 设置搜索方式  matchAllQuery：全部搜索
        sourceBuilder.query(QueryBuilders.matchAllQuery());
        // 设置源字段过滤,第一个参数结果集要显示哪些字段，第二个参数结果集不显示哪些参数
        sourceBuilder.fetchSource(new String[]{"name", "studymodel"}, new String[]{});
        // 向搜索对象设置搜索源对象
        searchRequest.source(sourceBuilder);
        // 执行搜索
        SearchResponse searchResponse = client.search(searchRequest);
        // 拿到结果集
        SearchHits hits = searchResponse.getHits();
        // 匹配到的总记录数
        long totalHits = hits.getTotalHits();
        // 最终结果集
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            String index = hit.getIndex();
            String type = hit.getType();
            // 主键
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            // 源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            // String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            //System.out.println(description);
        }


    }

    /**
     * 分页搜索
     *
     * @throws IOException
     */
    @Test
    public void testSearchPage() throws IOException {
        // 构建搜索对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        // 指定类型
        searchRequest.types("doc");
        // 构建搜索源对象
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 跳过多少条 跟mysql一样
        sourceBuilder.from(1);
        // 每次显示多少条
        sourceBuilder.size(1);
        // 设置搜索方式  matchAllQuery：全部搜索
        sourceBuilder.query(QueryBuilders.matchAllQuery());
        // 设置源字段过滤,第一个参数结果集要显示哪些字段，第二个参数结果集不显示哪些参数
        sourceBuilder.fetchSource(new String[]{"name", "studymodel"}, new String[]{});
        // 向搜索对象设置搜索源对象
        searchRequest.source(sourceBuilder);
        // 执行搜索
        SearchResponse searchResponse = client.search(searchRequest);
        // 拿到结果集
        SearchHits hits = searchResponse.getHits();
        // 匹配到的总记录数
        long totalHits = hits.getTotalHits();
        // 最终结果集
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            String index = hit.getIndex();
            String type = hit.getType();
            // 主键
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            // 源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            // String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            //System.out.println(description);
        }


    }

    /**
     * 精确匹配搜索，不分词
     *
     * @throws IOException
     */
    @Test
    public void testTremQuery() throws IOException {
        // 构建搜索对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        // 指定类型
        searchRequest.types("doc");
        // 构建搜索源对象
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 设置搜索方式  termQuery：精确搜索 termsQuery：多个精确搜索
        sourceBuilder.query(QueryBuilders.termQuery("name", "spring"));
        // 设置源字段过滤,第一个参数结果集要显示哪些字段，第二个参数结果集不显示哪些参数
        sourceBuilder.fetchSource(new String[]{"name", "studymodel"}, new String[]{});
        // 向搜索对象设置搜索源对象
        searchRequest.source(sourceBuilder);
        // 执行搜索
        SearchResponse searchResponse = client.search(searchRequest);
        // 拿到结果集
        SearchHits hits = searchResponse.getHits();
        // 匹配到的总记录数
        long totalHits = hits.getTotalHits();
        // 最终结果集
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            String index = hit.getIndex();
            String type = hit.getType();
            // 主键
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            // 源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            // String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            //System.out.println(description);
        }


    }

    /**
     * 根据ID多个精确匹配搜索，不分词
     *
     * @throws IOException
     */
    @Test
    public void testTremQueryByIds() throws IOException {
        // 构建搜索对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        // 指定类型
        searchRequest.types("doc");
        // 构建搜索源对象
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 设置搜索方式  termQuery：精确搜索 termsQuery：多个精确搜索
        // 定义id数组
        String[] str = new String[]{"1", "2"};
        sourceBuilder.query(QueryBuilders.termsQuery("_id", str));
        // 设置源字段过滤,第一个参数结果集要显示哪些字段，第二个参数结果集不显示哪些参数
        sourceBuilder.fetchSource(new String[]{"name", "studymodel"}, new String[]{});
        // 向搜索对象设置搜索源对象
        searchRequest.source(sourceBuilder);
        // 执行搜索
        SearchResponse searchResponse = client.search(searchRequest);
        // 拿到结果集
        SearchHits hits = searchResponse.getHits();
        // 匹配到的总记录数
        long totalHits = hits.getTotalHits();
        // 最终结果集
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            String index = hit.getIndex();
            String type = hit.getType();
            // 主键
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            // 源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            // String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            //System.out.println(description);
        }


    }

    /**
     * mathQuery是先将搜索字符串分词，再使用各各词条从索引中搜索
     * minimumShouldMatch 设置分词搜索到的站百分之70以上 3*0.7 向上取整
     *
     * @throws IOException
     */
    @Test
    public void testMathQuery() throws IOException {
        // 构建搜索对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        // 指定类型
        searchRequest.types("doc");
        // 构建搜索源对象
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 设置搜索方式
        // 定义id数组
        sourceBuilder.query(QueryBuilders.matchQuery("description", "spring开发框架")
                .minimumShouldMatch("70%"));
        // 设置源字段过滤,第一个参数结果集要显示哪些字段，第二个参数结果集不显示哪些参数
        sourceBuilder.fetchSource(new String[]{"name", "studymodel"}, new String[]{});
        // 向搜索对象设置搜索源对象
        searchRequest.source(sourceBuilder);
        // 执行搜索
        SearchResponse searchResponse = client.search(searchRequest);
        // 拿到结果集
        SearchHits hits = searchResponse.getHits();
        // 匹配到的总记录数
        long totalHits = hits.getTotalHits();
        // 最终结果集
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            String index = hit.getIndex();
            String type = hit.getType();
            // 主键
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            // 源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            // String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            //System.out.println(description);
        }
    }

    /**
     * muit_MathQuery 多个列匹配
     * field("name",10)： name的匹配得分权重乘以10
     * @throws IOException
     */
    @Test
    public void testMuitMathQuery() throws IOException {
        // 构建搜索对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        // 指定类型
        searchRequest.types("doc");
        // 构建搜索源对象
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 设置搜索方式
        // 定义id数组
        sourceBuilder.query(QueryBuilders.multiMatchQuery("spring css","name","description")
                                        .minimumShouldMatch("50%").field("name",10));
        // 设置源字段过滤,第一个参数结果集要显示哪些字段，第二个参数结果集不显示哪些参数
        sourceBuilder.fetchSource(new String[]{"name", "studymodel"}, new String[]{});
        // 向搜索对象设置搜索源对象
        searchRequest.source(sourceBuilder);
        // 执行搜索
        SearchResponse searchResponse = client.search(searchRequest);
        // 拿到结果集
        SearchHits hits = searchResponse.getHits();
        // 匹配到的总记录数
        long totalHits = hits.getTotalHits();
        // 最终结果集
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            String index = hit.getIndex();
            String type = hit.getType();
            // 主键
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            // 源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            // String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            //System.out.println(description);
        }


    }


    /**
     * BoolQuery 布尔查询
     * @throws IOException
     */
    @Test
    public void testBoolQuery() throws IOException {
        // 构建搜索对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        // 指定类型
        searchRequest.types("doc");
        // 构建搜索源对象
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 设置搜索方式
        // 1.定义MultiMatchQuery对象
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("spring css", "name", "description")
                .minimumShouldMatch("50%").field("name", 10);
        // 2. 定义termQuery对象
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("studymodel", "201001");
        // 3.定义BoolQuery布尔对象
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        // 4.把条件放到布尔对象中
        boolQueryBuilder.must(multiMatchQueryBuilder);
        boolQueryBuilder.must(termQueryBuilder);
        sourceBuilder.query(boolQueryBuilder);
        // 设置源字段过滤,第一个参数结果集要显示哪些字段，第二个参数结果集不显示哪些参数
        sourceBuilder.fetchSource(new String[]{"name", "studymodel"}, new String[]{});
        // 向搜索对象设置搜索源对象
        searchRequest.source(sourceBuilder);
        // 执行搜索
        SearchResponse searchResponse = client.search(searchRequest);
        // 拿到结果集
        SearchHits hits = searchResponse.getHits();
        // 匹配到的总记录数
        long totalHits = hits.getTotalHits();
        // 最终结果集
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            String index = hit.getIndex();
            String type = hit.getType();
            // 主键
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            // 源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            System.out.println(name);
            System.out.println(studymodel);
        }


    }


    /**
     * Sort 排序 desc asc
     * @throws IOException
     */
    @Test
    public void testSort() throws IOException {
        // 构建搜索对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        // 指定类型
        searchRequest.types("doc");
        // 构建搜索源对象
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 设置搜索方式
        // 定义BoolQuery布尔对象
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        // 定义过滤器对象
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(0).lte(100));
        // 根据布尔对象放到搜索源对象中
        sourceBuilder.query(boolQueryBuilder);
        sourceBuilder.sort("studymodel", SortOrder.DESC);
        sourceBuilder.sort("price", SortOrder.ASC);

        // 设置源字段过滤,第一个参数结果集要显示哪些字段，第二个参数结果集不显示哪些参数
        sourceBuilder.fetchSource(new String[]{"name", "studymodel"}, new String[]{});
        // 向搜索对象设置搜索源对象
        searchRequest.source(sourceBuilder);
        // 执行搜索
        SearchResponse searchResponse = client.search(searchRequest);
        // 拿到结果集
        SearchHits hits = searchResponse.getHits();
        // 匹配到的总记录数
        long totalHits = hits.getTotalHits();
        // 最终结果集
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            String index = hit.getIndex();
            String type = hit.getType();
            // 主键
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            // 源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            System.out.println(name);
            System.out.println(studymodel);
        }
    }

    /**
     * filter 过滤器，是对结果集过滤，性能比较高
     * @throws IOException
     */
    @Test
    public void testFilter() throws IOException {
        // 构建搜索对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        // 指定类型
        searchRequest.types("doc");
        // 构建搜索源对象
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 设置布尔搜索方式
        // 1.定义MultiMatchQuery对象
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("spring css", "name", "description")
                .minimumShouldMatch("50%").field("name", 10);
        // 2. 定义termQuery对象
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("studymodel", "201001");
        // 3.定义BoolQuery布尔对象
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        // 4.把条件放到布尔对象中
        boolQueryBuilder.must(multiMatchQueryBuilder);
        // 定义过滤器对象
        boolQueryBuilder.filter(QueryBuilders.termQuery("studymodel","201001"));
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(80).lte(100));

        sourceBuilder.query(boolQueryBuilder);
        // 设置源字段过滤,第一个参数结果集要显示哪些字段，第二个参数结果集不显示哪些参数
        sourceBuilder.fetchSource(new String[]{"name", "studymodel"}, new String[]{});
        // 向搜索对象设置搜索源对象
        searchRequest.source(sourceBuilder);
        // 执行搜索
        SearchResponse searchResponse = client.search(searchRequest);
        // 拿到结果集
        SearchHits hits = searchResponse.getHits();
        // 匹配到的总记录数
        long totalHits = hits.getTotalHits();
        // 最终结果集
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            String index = hit.getIndex();
            String type = hit.getType();
            // 主键
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            // 源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            System.out.println(name);
            System.out.println(studymodel);
        }
    }



    /**
     * Highlight 高亮显示，有点麻烦
     * @throws IOException
     */
    @Test
    public void testHighlight() throws IOException {
        // 构建搜索对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        // 指定类型
        searchRequest.types("doc");
        // 构建搜索源对象
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 设置布尔搜索方式
        // 1.定义MultiMatchQuery对象
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("开发框架", "name", "description")
                .minimumShouldMatch("50%").field("name", 10);
        // 3.定义BoolQuery布尔对象
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        // 4.把条件放到布尔对象中,.must代表并且
        boolQueryBuilder.must(multiMatchQueryBuilder);
        // 添加过滤器
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(0).lte(100));
        sourceBuilder.query(boolQueryBuilder);

        // 高亮对象
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<tag>");      // 设置前缀
        highlightBuilder.postTags("</tag>");   // 设置后缀
        // 设置高亮字段
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        highlightBuilder.fields().add(new HighlightBuilder.Field("description"));
        sourceBuilder.highlighter(highlightBuilder);
        // 设置源字段过滤,第一个参数结果集要显示哪些字段，第二个参数结果集不显示哪些参数
        sourceBuilder.fetchSource(new String[]{"name","studymodel","price","description"}, new String[]{});
        // 向搜索对象设置搜索源对象
        searchRequest.source(sourceBuilder);
        // 执行搜索
        SearchResponse searchResponse = client.search(searchRequest);
        // 拿到结果集
        SearchHits hits = searchResponse.getHits();
        // 匹配到的总记录数
        long totalHits = hits.getTotalHits();
        // 最终结果集
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            // 主键
            String id = hit.getId();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            // 源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            // 获取结果中的高亮对象
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (highlightBuilder != null ){
                // 取name的
                HighlightField nameHighlightField = highlightFields.get("name");
                if (nameHighlightField != null) {
                Text[] fragments = nameHighlightField.getFragments();
                StringBuilder stringBuilder = new StringBuilder();
                    for (Text text : fragments) {
                        stringBuilder.append(text);
                    }
                    name = stringBuilder.toString();
                }
            }
            String studymodel = (String) sourceAsMap.get("studymodel");
            System.out.println(name);
            System.out.println(studymodel);
        }


    }





}