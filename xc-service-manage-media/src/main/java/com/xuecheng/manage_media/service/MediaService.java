package com.xuecheng.manage_media.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

/**
 * @Author: xiongwei
 * @Date: 2019/11/27
 * @why：
 */
@Service
public class MediaService {

    @Autowired
    private MediaFileRepository mediaFileRepository;

    @Value("${xc-service-manage-media.upload-location}")
    private String upload_location;

    @Value("${xc-service-manage-media.mq.routingkey-media-video}")
    private String routingkey_media_video;

    @Autowired
    private RabbitTemplate rabbitTemplate;



    /**
     * 文件注册
     * @return
     */
    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String
            mimetype, String fileExt){
        // 检查文件是否上传
        // 1.得到文件路径
        String filePath = getFilePath(fileMd5,fileExt);
        File file = new File(filePath);
        // 2.检查数据库文件是否存在
        Optional<MediaFile> mediaFile = mediaFileRepository.findById(fileMd5);
        // 校验文件是否已经注册
        if (file.exists() && mediaFile.isPresent()) {
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
        }
        boolean foldePath = createFileFolderPath(fileMd5);
        if (!foldePath) {
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_FAIL);
        }
        return ResponseResult.SUCCESS();

    }


    /**
     * 分块检查
     * @param fileMd5
     * @param chunk
     * @param chunkSize
     * @return
     */
    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize) {
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        File file = new File(chunkFileFolderPath+chunk);
        if (file.exists()) {
            // 存在
            return new CheckChunkResult(CommonCode.SUCCESS,true);
        } else {
            // 分块不存在
            return new CheckChunkResult(CommonCode.SUCCESS,false);
        }
    }

    /**
     * 块文件上传
     * @param file
     * @param fileMd5
     * @param chunk
     * @return
     */
    public ResponseResult uploadchunk(MultipartFile file, String fileMd5, Integer chunk) {
        // 得到分块文件所属目录
        String folderPath = this.getChunkFileFolderPath(fileMd5);
        File chunlfile= new File(folderPath);
        // 没有路径，创建路径
        if (!chunlfile.exists()) {
            chunlfile.mkdirs();
        }
        InputStream inputStream = null;
        // 输出流
        FileOutputStream outputStream = null;
        try {
            // 得到分块文件
            inputStream = file.getInputStream();
            // 输出到块文件
            outputStream = new FileOutputStream(folderPath+chunk);
            IOUtils.copy(inputStream,outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return ResponseResult.SUCCESS();
    }

    /**
     * 合并并且校验文件
     * @param fileMd5
     * @param fileName
     * @param fileSize
     * @param mimetype
     * @param fileExt
     * @return
     */
    public ResponseResult mergechunks(String fileMd5, String fileName, Long fileSize, String
            mimetype, String fileExt) {

        // 1.先合并文件
        // 分块文件目录
        String chunkfiledolder = this.getChunkFileFolderPath(fileMd5);
        File file = new File(chunkfiledolder);
        // 分块文件集合
        File[] listFiles = file.listFiles();
        List<File> fileList= Arrays.asList(listFiles);

        // 创建一个合并文件
        String filePath = this.getFilePath(fileMd5, fileExt);
        File mergeFile = new File(filePath);
        // 执行合并
        mergeFile = this.mergeFile(fileList, mergeFile);
        if (mergeFile == null) {
            ExceptionCast.cast(MediaCode.MERGE_FILE_FAIL);
        }

        // 2.验证文件MD5与前端传的值是否一致
        this.checkFileMD5(mergeFile,fileMd5);

        // 3.将文件信息写入mongodb
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileName(fileMd5+"."+fileExt);
        mediaFile.setFileOriginalName(fileName);
        //文件路径保存相对路径
        String folderPath = fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" ;
        mediaFile.setFilePath(folderPath);
        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimeType(mimetype);
        mediaFile.setFileType(fileExt);
        //状态为上传成功
        mediaFile.setFileStatus("301002");
        MediaFile save = mediaFileRepository.save(mediaFile);
        // 向MQ发送消息
        ResponseResult responseResult = this.sendProcessVideoMsg(mediaFile.getFileId());
        return responseResult;
    }

    /**
     * 发送MQ消息
     * @param mediaId 文件ID
     */
    private ResponseResult sendProcessVideoMsg(String mediaId) {
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if(!optional.isPresent()){
            return new ResponseResult(CommonCode.FAIL);
        }
        //发送视频处理消息
        Map<String,String> msgMap = new HashMap<>(16);
        msgMap.put("mediaId",mediaId);
        //发送的消息
        String msg = JSON.toJSONString(msgMap);
        try {
            this.rabbitTemplate.convertAndSend(RabbitMQConfig.EX_MEDIA_PROCESSTASK,routingkey_media_video,
                    msg);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseResult(CommonCode.FAIL);
        }
        return new ResponseResult(CommonCode.SUCCESS);

    }

    /**
     * 校验文件
     * @param mergeFile
     * @param fileMd5
     */
    private void checkFileMD5(File mergeFile, String fileMd5) {
        try {
            FileInputStream fileInputStream = new FileInputStream(mergeFile);
            String md5Hex = DigestUtils.md5Hex(fileInputStream);
            if (!md5Hex.equalsIgnoreCase(fileMd5)) {
                ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行合并
     * @param fileList
     * @param mergeFile
     * @return
     */
    private File mergeFile(List<File> fileList, File mergeFile) {
        // 合并文件存在就删除
        try {
            if (mergeFile.exists()) {
                mergeFile.delete();
            } else {
                mergeFile.createNewFile();
            }
            // 文件排序
            Collections.sort(fileList,new Comparator<File>(){
                @Override
                public int compare(File o1, File o2) {
                    if (Integer.parseInt(o1.getName()) > Integer.parseInt(o2.getName())) {
                        return 1;
                    }
                    return -1;
                }
            });
            RandomAccessFile raf_write = new RandomAccessFile(mergeFile,"rw");
            byte[] b = new byte[1024];
            for (File chunkFile : fileList) {
                RandomAccessFile raf_read = new RandomAccessFile(chunkFile,"r");
                int len = -1;
                while ((len = raf_read.read(b)) != -1 ) {
                    raf_write.write(b,0,len);
                }
                raf_read.close();
            }
            raf_write.close();
            return mergeFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 得到分块文件目录
     */
    private String getChunkFileFolderPath(String fileMd5) {
        return  upload_location + fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/"
                + fileMd5 + "/chunk/";
    }

    /**
     * 创建文件目录
     * @param fileMd5
     * @return
     */
    private boolean createFileFolderPath(String fileMd5) {
        String folder =  getFileFolderPath(fileMd5);
        File file = new File(folder);
        if (!file.exists()) {
            boolean mkdirs = file.mkdirs();
            return mkdirs;
        }
            return true;
    }

    /**
     * 得到文件目录
     */
    private String getFileFolderPath(String fileMd5) {
        return  upload_location + fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/"
                + fileMd5 + "/" ;
    }


    /**
     * 根据文件md5得到文件路径
     * 规则：
     * 一级目录：md5的第一个字符
     * 二级目录：md5的第二个字符
     * 三级目录：md5
     * 文件名：md5+文件扩展名
     * @param fileMd5 文件md5值
     * @param fileExt 文件扩展名
     * @return 文件路径
     */
    private String getFilePath(String fileMd5, String fileExt) {
        return upload_location + fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/"
                + fileMd5 + "/" + fileMd5 + "." + fileExt;
    }


}
