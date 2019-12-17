package com.xuecheng.order.service;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.order.dao.XcTaskHisRepository;
import com.xuecheng.order.dao.XcTaskRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @Author: xiongwei
 * @Date: 2019/12/16
 * @why：
 */
@Service
public class TaskService {

    @Autowired
    private XcTaskRepository xcTaskRepository;

    @Autowired
    private XcTaskHisRepository xcTaskHisRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 取出前n条任务,取出指定时间之前处理的任务
     *
     * @param updateTime
     * @param size
     * @return
     */
    public List<XcTask> findTaskList(Date updateTime, int size) {
        //设置分页参数，取出前n 条记录
        Pageable pageable = new PageRequest(0, size);
        Page<XcTask> xcTasks = xcTaskRepository.findByUpdateTimeBefore(pageable, updateTime);
        return xcTasks.getContent();
    }

    /**
     * 发送消息
     * @param xcTask     任务对象
     * @param ex         交换机id
     * @param routingKey
     */
    @Transactional(rollbackFor = Exception.class)
    public void publish(XcTask xcTask, String ex, String routingKey) {
        //查询任务
        Optional<XcTask> taskOptional = xcTaskRepository.findById(xcTask.getId());
        if (taskOptional.isPresent()) {
            XcTask one = taskOptional.get();
            // 发送消息
            rabbitTemplate.convertAndSend(ex, routingKey, one);
            //更新任务时间为当前时间
            xcTask.setUpdateTime(new Date());
            xcTaskRepository.save(xcTask);
        }
    }


    /**
     * 取出任务
     * @param taskId
     * @param version
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public int getTask(String taskId,int version){
        // 采用乐观锁来处理
        int i = xcTaskRepository.updateTaskVersion(taskId, version);
        return i;
    }



    /**
     * 删除任务
     * @param taskId
     */
    @Transactional(rollbackFor = Exception.class)
    public void finishTask(String taskId){
        Optional<XcTask> taskOptional = xcTaskRepository.findById(taskId);
        if(taskOptional.isPresent()){
            XcTask xcTask = taskOptional.get();
            xcTask.setDeleteTime(new Date());
            XcTaskHis xcTaskHis = new XcTaskHis();
            BeanUtils.copyProperties(xcTask, xcTaskHis);
            xcTaskHisRepository.save(xcTaskHis);
            xcTaskRepository.delete(xcTask);
        }
    }


}
