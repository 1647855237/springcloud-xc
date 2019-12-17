package com.xuecheng.manage_media_process;

import com.xuecheng.framework.utils.Mp4VideoUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 使用processBuilder调用第三方程序
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestProcessBuilder {

    @Test
    public void testProcessBuilder() throws IOException {
        // 创建对象
        ProcessBuilder processBuilder = new ProcessBuilder();
        // 设置第三方的命令
        processBuilder.command("ping", "127.0.0.1");

        // 设置正常和错误流合并
        processBuilder.redirectErrorStream(true);

        // 启动一个进程
        Process process = processBuilder.start();

        // 通过标准流得到正常流和错误流，还需要转成字符流
        InputStream inputStream = process.getInputStream();
        // 转字符流
        InputStreamReader reader = new InputStreamReader(inputStream, "gbk");

        char[] chars = new char[1024];
        int b = -1;
        while ((b = reader.read(chars)) != -1) {
            String s = new String(chars, 0, b);
            System.out.println(s);
        }


    }

    //测试使用工具类将avi转成mp4
    @Test
    public void testProcessMp4() {
        //ffmpeg的路径
        String ffmpeg_path = "D:\\improt\\ffmpeg-20180227-fa0c9d6-win64-static\\bin\\ffmpeg.exe";
        //源avi视频的路径
        String video_path = "D:\\xuecheng\\lucene.avi";
        //转换后mp4文件的名称
        String mp4_name = "lucene.mp4";
        //转换后mp4文件的路径
        String mp4_path = "D:/xuecheng/ffmpegtest/";
        //创建工具类对象
        Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpeg_path, video_path, mp4_name, mp4_path);
        //开始视频转换，成功将返回success
        String s = videoUtil.generateMp4();
        System.out.println(s);
    }

}
