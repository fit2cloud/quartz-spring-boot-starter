package com.fit2cloud.autoconfigure;


import com.fit2cloud.autoconfigure.anno.QuartzScheduled;
import com.fit2cloud.autoconfigure.config.ClusterQuartzFixedDelayJobBean;
import com.fit2cloud.autoconfigure.config.ClusterQuartzJobBean;
import com.fit2cloud.autoconfigure.config.FixedDelayJobData;
import com.fit2cloud.autoconfigure.config.FixedDelayJobListener;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 这里必须用 ApplicationContextAware,
 * `@EventListener ContextRefreshedEvent` 的顺序在 ApplicationContextAware 之后，
 * <pre>
 * Spring 实例化过程
 * 1. 实例化;
 * 2. 设置属性值;
 * 3. 如果实现了BeanNameAware接口,调用setBeanName设置Bean的ID或者Name;
 * 4. 如果实现BeanFactoryAware接口,调用setBeanFactory 设置BeanFactory;
 * 5. 如果实现ApplicationContextAware,调用setApplicationContext设置ApplicationContext
 * 6. 调用BeanPostProcessor的预先初始化方法;
 * 7. 调用InitializingBean的afterPropertiesSet()方法;
 * 8. 调用定制init-method方法；
 * 9. 调用BeanPostProcessor的后初始化方法;
 * 10. @EventListener ContextRefreshedEvent
 * </pre>
 */
public class SchedulerStarter implements BeanPostProcessor, ApplicationContextAware {
    private DateTime now;
    @Resource
    private Scheduler scheduler;

    private Map<String, JobDetailTrigger> jobDetailTriggerMap = new HashMap<>();

    private ConfigurableApplicationContext applicationContext;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Method[] methods = ReflectionUtils.getAllDeclaredMethods(bean.getClass());
        if (methods != null) {
            for (Method method : methods) {
                QuartzScheduled annotation = AnnotationUtils.findAnnotation(method, QuartzScheduled.class);
                if (annotation != null) {
                    JobDataMap jobDataMap = new JobDataMap();
                    jobDataMap.put("targetObject", beanName);
                    jobDataMap.put("targetMethod", method.getName());
                    String cron = annotation.cron();
                    long fixedDelay = annotation.fixedDelay();
                    long fixedRate = annotation.fixedRate();
                    int initialDelay = (int) annotation.initialDelay();
                    final JobDetail jobDetail;
                    final Trigger trigger;
                    String jobDetailIdentity = beanName + "." + method.getName();
                    if (StringUtils.isNotBlank(cron)) {
                        cron = getCronExpression(cron);
                        jobDetail = JobBuilder.newJob(ClusterQuartzJobBean.class)
                                .storeDurably(true).usingJobData(jobDataMap).build();
                        trigger = TriggerBuilder.newTrigger().withIdentity(jobDetailIdentity)
                                .startAt(now.plusMillis(initialDelay).toDate())
                                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                                .build();
                    } else if (fixedDelay > 0) {
                        jobDataMap.put(FixedDelayJobListener.FIXED_DELAY_JOB_DATA, new FixedDelayJobData(fixedDelay));
                        jobDetail = JobBuilder.newJob(ClusterQuartzFixedDelayJobBean.class)
                                .storeDurably(true).usingJobData(jobDataMap).build();
                        trigger = TriggerBuilder.newTrigger().withIdentity(jobDetailIdentity)
                                .startAt(now.plusMillis(initialDelay).toDate())
                                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(fixedDelay).repeatForever())
                                .build();
                    } else {
                        jobDetail = JobBuilder.newJob(ClusterQuartzJobBean.class)
                                .storeDurably(true).usingJobData(jobDataMap).build();
                        trigger = TriggerBuilder.newTrigger().withIdentity(jobDetailIdentity)
                                .startAt(now.plusMillis(initialDelay).toDate())
                                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                                        .withIntervalInMilliseconds(fixedRate).repeatForever())
                                .build();
                    }
                    jobDetailTriggerMap.put(jobDetailIdentity, new JobDetailTrigger(jobDetail, trigger));
                }
            }
        }
        return bean;
    }

    private String getCronExpression(String cron) {
        cron = cron.trim();
        if (StringUtils.startsWith(cron, "${") && StringUtils.endsWith(cron, "}")) {
            return applicationContext.getBeanFactory().resolveEmbeddedValue(cron);
        }
        return cron;
    }

    /**
     * spring 完全刷新之后执行
     *
     * @param event
     * @throws BeansException
     */
    @EventListener
    public void startScheduler(ContextRefreshedEvent event) throws BeansException {
        try {
            scheduler.deleteJobs(getJobKeys());
            scheduler.unscheduleJobs(getTriggerKeys());
            scheduler.getListenerManager().addJobListener(new FixedDelayJobListener());
            for (String jobDetailIdentity : jobDetailTriggerMap.keySet()) {
                JobDetailTrigger jobDetailTrigger = this.jobDetailTriggerMap.get(jobDetailIdentity);
                scheduler.scheduleJob(jobDetailTrigger.jobDetail, jobDetailTrigger.trigger);
            }
            if (!scheduler.isShutdown()) {
                scheduler.start();
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取数据库中的所有JobKey
     *
     * @return JobKey列表
     * @throws SchedulerException
     */
    private List<JobKey> getJobKeys() throws SchedulerException {
        List<String> jobGroupNames = scheduler.getJobGroupNames();
        List<JobKey> jobKeys = new ArrayList<>();
        for (String jobGroupName : jobGroupNames) {
            jobKeys.addAll(scheduler.getJobKeys(GroupMatcher.jobGroupEquals(jobGroupName)));
        }
        return jobKeys;
    }

    /**
     * 获取数据库中的所有TriggerKey
     *
     * @return TriggerKey列表
     * @throws SchedulerException
     */
    private List<TriggerKey> getTriggerKeys() throws SchedulerException {
        List<String> triggerGroupNames = scheduler.getJobGroupNames();
        List<TriggerKey> triggerKeys = new ArrayList<>();
        for (String triggerGroupName : triggerGroupNames) {
            triggerKeys.addAll(scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(triggerGroupName)));
        }
        return triggerKeys;
    }

    /**
     * 容器初始化好之后执行
     *
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        now = DateTime.now();
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    private static class JobDetailTrigger {
        JobDetail jobDetail;
        Trigger trigger;

        JobDetailTrigger(JobDetail jobDetail, Trigger trigger) {
            this.jobDetail = jobDetail;
            this.trigger = trigger;
        }
    }
}
