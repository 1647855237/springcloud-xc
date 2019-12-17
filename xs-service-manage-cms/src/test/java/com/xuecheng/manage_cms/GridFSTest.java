package com.xuecheng.manage_cms;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @Author: xiongwei
 * @Date: 2019/8/21
 * @why：
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class GridFSTest {

    @Autowired
    private GridFsTemplate gridFSTemplate;

    @Autowired
    private GridFSBucket gridFSBucket;

    /**
     * 存
     * @throws FileNotFoundException
     */
    @Test
    public void GridFsTest() throws FileNotFoundException {
        /**
         * 这个使用得配置mongodb，可以在使用mongodb的地方使用
         */
        File file = new File("D:/images/index_banner.ftl");

        FileInputStream inputStream = new FileInputStream(file);

        ObjectId objectId = gridFSTemplate.store(inputStream, "轮播测试", "");

        System.out.println(objectId.toString());
    }

    /**
     * 取
     * @throws IOException
     */
    @Test
    public void GridFsQueryFIle() throws IOException {
        String fileId = "5d5cc6d83533c36ab09a9165";
        //根据id查询文件
        GridFSFile gridFSFile =
                gridFSTemplate.findOne(Query.query(Criteria.where("_id").is(fileId)));
        //打开下载流对象
        GridFSDownloadStream gridFSDownloadStream =
                gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        //创建gridFsResource，用于获取流对象
        GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
        //获取流中的数据
        String s = IOUtils.toString(gridFsResource.getInputStream(), "UTF-8");
        System.out.println(s);



    }


}
