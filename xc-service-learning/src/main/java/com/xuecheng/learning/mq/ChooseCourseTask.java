package com.xuecheng.learning.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.learning.config.RabbitMQConfig;
import com.xuecheng.learning.service.LearningService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @Author: xiongwei
 * @Date: 2019/12/16
 * @why： 监听mq消息，完成自动选课
 */
@Component
public class ChooseCourseTask {

    @Autowired
    LearningService learningService;
    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 接收选课任务
     */
    @RabbitListener(queues = {RabbitMQConfig.XC_LEARNING_ADDCHOOSECOURSE})
    public void receiveChoosecourseTask(XcTask xcTask) throws ParseException {

        String requestBody = xcTask.getRequestBody();
        Map map = JSON.parseObject(requestBody, Map.class);
        String userId = (String) map.get("userId");
        String courseId = (String) map.get("courseId");
        String valid = (String) map.get("valid");
        Date startTime = null;
        Date endTime = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        if (map.get("startTime") != null) {
            startTime = dateFormat.parse((String) map.get("startTime"));
        }
        if (map.get("endTime") != null) {
            endTime = dateFormat.parse((String) map.get("endTime"));
        }
        //添加选课
        ResponseResult addcourse = learningService.addcourse(userId, courseId,
                null, null, null, xcTask);
        // 添加选课成功后向mq发送消息
        if (addcourse.isSuccess()) {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EX_LEARNING_CHOOSECOURSE,
                    RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE_KEY, xcTask);
        }
    }
}