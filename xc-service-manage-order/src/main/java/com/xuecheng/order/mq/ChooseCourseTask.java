package com.xuecheng.order.mq;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.config.RabbitMQConfig;
import com.xuecheng.order.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @Author: xiongwei
 * @Date: 2019/12/16
 * @why：定时任务类
 */
@Component
@Slf4j
public class ChooseCourseTask {

    @Autowired
    TaskService taskService;


    /**
     * 监听选课完成消息
     * @param xcTask
     */
    @RabbitListener(queues = {RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE})
    public void receiveChoosecourseTask(XcTask xcTask){
        if (xcTask != null && StringUtils.isNotEmpty(xcTask.getId())) {
            taskService.finishTask(xcTask.getId());
        }

    }
    
    

    /**
     * 每隔1分钟扫描消息表，向mq发送消息
     */
    @Scheduled(cron="0/3 * * * * *")
    public void sendChoosecourseTask(){
        //取出当前时间1分钟之前的时间
        Calendar calendar =new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(GregorianCalendar.MINUTE,-1);
        Date time = calendar.getTime();
        List<XcTask> taskList = taskService.findTaskList(time, 1000);
        // 向mq发送消息
        for (XcTask xcTask : taskList) {
            if (taskService.getTask(xcTask.getId(),xcTask.getVersion())> 0) {
                String ex = xcTask.getMqExchange();
                String routingkey = xcTask.getMqRoutingkey();
                taskService.publish(xcTask,ex,routingkey);
            }
        }
    }



    /**
     * Scheduled这个是使用的spring task任务调度，还有一个是Quartz异步调度框架
     * Scheduled默认是串行执行的，一个执行完毕另一个才会执行，而且它默认是方法
     * 内容执行完毕后才开始执行下一个任务，所以不用担心未执行完又开始执行任务了
     */
    //@Scheduled(fixedRate = 5000) //上次执行开始时间后5秒执行
    // @Scheduled(fixedDelay = 5000) //上次执行完毕后5秒执行
    // @Scheduled(initialDelay=3000, fixedRate=5000) //第一次延迟3秒，以后每隔5秒执行一次
    //@Scheduled(cron="0/3 * * * * *")//每隔3秒执行一次
    public void task1(){
        log.info("===============测试定时任务1开始===============");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("===============测试定时任务1结束===============");
    }


}
