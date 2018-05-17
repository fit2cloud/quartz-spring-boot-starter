package com.fit2cloud.autoconfigure.service;

import com.fit2cloud.autoconfigure.config.ClusterQuartzJobBean;
import org.quartz.*;

import javax.annotation.Resource;
import java.util.List;

public class QuartzManageService {
    @Resource
    private Scheduler scheduler;

    public List<JobExecutionContext> getCurrentlyExecutingJobs() throws Exception {
        return scheduler.getCurrentlyExecutingJobs();
    }

    public void deleteJob(JobKey jobKey) throws Exception {
        scheduler.deleteJob(jobKey);
    }

    public void pauseJob(JobKey jobKey) throws Exception {
        scheduler.pauseJob(jobKey);
    }

    public void resumeJob(JobKey jobKey) throws Exception {
        scheduler.resumeJob(jobKey);
    }

    public void rescheduleJob(TriggerKey triggerKey, Trigger newTrigger) throws Exception {
        scheduler.rescheduleJob(triggerKey, newTrigger);
    }

    public void addJob(JobDetail jobDetail, Trigger trigger) throws Exception {
        scheduler.scheduleJob(jobDetail, trigger);
    }

    public void addJob(JobDataMap jobDataMap, Trigger trigger) throws Exception {
        JobDetail jobDetail = JobBuilder.newJob(ClusterQuartzJobBean.class)
                .storeDurably(true).usingJobData(jobDataMap).build();
        addJob(jobDetail, trigger);
    }

    public void addJob(String beanName, String methodName, Trigger trigger) throws Exception {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("targetObject", beanName);
        jobDataMap.put("targetMethod", methodName);
        addJob(jobDataMap, trigger);
    }

    public void addJob(String beanName, String methodName, String cron) throws Exception {
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity(beanName + methodName)
                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                .build();
        addJob(beanName, methodName, trigger);
    }
}
