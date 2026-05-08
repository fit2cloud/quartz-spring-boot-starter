package com.fit2cloud.quartz;


import com.fit2cloud.autoconfigure.QuartzProperties;
import com.fit2cloud.quartz.anno.QuartzScheduled;
import com.fit2cloud.quartz.config.ClusterQuartzFixedDelayJobBean;
import com.fit2cloud.quartz.config.ClusterQuartzJobBean;
import com.fit2cloud.quartz.config.FixedDelayJobData;
import com.fit2cloud.quartz.config.FixedDelayJobListener;
import com.fit2cloud.quartz.service.QuartzManageService;
import com.fit2cloud.quartz.util.JobDetailTrigger;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.scheduling.SchedulingException;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

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
public class SchedulerStarter implements BeanPostProcessor, ApplicationContextAware, ApplicationRunner {
    private Logger logger = LoggerFactory.getLogger(SchedulerStarter.class);

    @Autowired
    private Scheduler scheduler;
    @Autowired
    private TimeZone quartzTimeZone;
    @Autowired
    private QuartzProperties quartzProperties;
    // Stores lazy suppliers so triggers are built with a fresh Instant.now() at actual schedule time
    private Map<String, Supplier<JobDetailTrigger>> jobDetailTriggerSupplierMap = new HashMap<>();

    private ConfigurableApplicationContext applicationContext;
    @Autowired
    private QuartzManageService quartzManageService;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Method[] methods = ReflectionUtils.getAllDeclaredMethods(bean.getClass());
        for (Method method : methods) {
            QuartzScheduled annotation = AnnotationUtils.findAnnotation(method, QuartzScheduled.class);
            if (annotation != null) {
                String resolvedCron = annotation.cron() != null && !annotation.cron().isEmpty()
                        ? getCronExpression(annotation.cron()) : null;
                long fixedDelay = annotation.fixedDelay();
                long fixedRate = annotation.fixedRate();
                long initialDelay = annotation.initialDelay();
                String jobDetailIdentity = beanName + "." + method.getName();

                // Build the trigger lazily at actual schedule time so startAt uses a fresh Instant.now()
                jobDetailTriggerSupplierMap.put(jobDetailIdentity, () -> {
                    Instant scheduleTime = Instant.now();
                    JobDataMap jobDataMap = new JobDataMap();
                    jobDataMap.put("targetObject", beanName);
                    jobDataMap.put("targetMethod", method.getName());
                    final JobDetail jobDetail;
                    final Trigger trigger;
                    if (resolvedCron != null && !resolvedCron.isEmpty()) {
                        jobDetail = JobBuilder.newJob(ClusterQuartzJobBean.class)
                                .storeDurably(true).usingJobData(jobDataMap).build();
                        trigger = TriggerBuilder.newTrigger()
                                .withIdentity(TriggerKey.triggerKey(jobDetailIdentity, quartzProperties.getGroupName()))
                                .startAt(new Date(scheduleTime.plusMillis(initialDelay).toEpochMilli()))
                                .withSchedule(CronScheduleBuilder.cronSchedule(resolvedCron)
                                        .inTimeZone(quartzTimeZone)
                                        // Skip missed fires instead of replaying them after downtime
                                        .withMisfireHandlingInstructionDoNothing())
                                .build();
                    } else if (fixedDelay > 0) {
                        jobDataMap.put(FixedDelayJobListener.FIXED_DELAY_JOB_DATA, new FixedDelayJobData(fixedDelay));
                        jobDetail = JobBuilder.newJob(ClusterQuartzFixedDelayJobBean.class)
                                .storeDurably(true).usingJobData(jobDataMap).build();
                        trigger = TriggerBuilder.newTrigger()
                                .withIdentity(TriggerKey.triggerKey(jobDetailIdentity, quartzProperties.getGroupName()))
                                .startAt(new Date(scheduleTime.plusMillis(initialDelay).toEpochMilli()))
                                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                                        .withIntervalInMilliseconds(fixedDelay).repeatForever()
                                        .withMisfireHandlingInstructionNextWithExistingCount())
                                .build();
                    } else {
                        jobDetail = JobBuilder.newJob(ClusterQuartzJobBean.class)
                                .storeDurably(true).usingJobData(jobDataMap).build();
                        trigger = TriggerBuilder.newTrigger()
                                .withIdentity(TriggerKey.triggerKey(jobDetailIdentity, quartzProperties.getGroupName()))
                                .startAt(new Date(scheduleTime.plusMillis(initialDelay).toEpochMilli()))
                                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                                        .withIntervalInMilliseconds(fixedRate).repeatForever()
                                        .withMisfireHandlingInstructionNextWithExistingCount())
                                .build();
                    }
                    return new JobDetailTrigger(jobDetail, trigger);
                });
            }
        }
        return bean;
    }

    private String getCronExpression(String cron) {
        cron = cron.trim();
        if (cron.startsWith("${") && cron.endsWith("}")) {
            return applicationContext.getBeanFactory().resolveEmbeddedValue(cron);
        }
        return cron;
    }

    /**
     * 获取数据库中的所有JobKey
     */
    private List<JobKey> getJobKeys() throws SchedulerException {
        return new ArrayList<>(scheduler.getJobKeys(GroupMatcher.jobGroupEquals(quartzProperties.getGroupName())));
    }

    /**
     * 获取数据库中的所有TriggerKey
     */
    private List<TriggerKey> getTriggerKeys() throws SchedulerException {
        return new ArrayList<>(scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(quartzProperties.getGroupName())));
    }

    /**
     * 容器初始化好之后执行
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    /**
     * spring 完全刷新之后执行
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            if (!scheduler.isShutdown()) {
                // Not using the Quartz startDelayed method since we explicitly want a daemon
                // thread here, not keeping the JVM alive in case of all other threads ending.
                Thread schedulerThread = new Thread(() -> {
                    try {
                        QuartzProperties quartzProperties = this.applicationContext.getBean(QuartzProperties.class);
                        TimeUnit.SECONDS.sleep(quartzProperties.getStartupDelay().getSeconds());
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        // simply proceed
                    }
                    try {
                        // Evaluate trigger suppliers now so startAt reflects the actual schedule time
                        Map<String, JobDetailTrigger> jobDetailTriggerMap = new HashMap<>();
                        for (Map.Entry<String, Supplier<JobDetailTrigger>> entry : jobDetailTriggerSupplierMap.entrySet()) {
                            jobDetailTriggerMap.put(entry.getKey(), entry.getValue().get());
                        }
                        // init all jobs
                        logger.info("reschedule all jobs...");
                        quartzManageService.rescheduleJobs(getJobKeys(), getTriggerKeys(), jobDetailTriggerMap);
                        scheduler.start();
                    } catch (Exception ex) {
                        throw new SchedulingException("Could not start Quartz Scheduler after delay", ex);
                    }
                });
                schedulerThread.setName("Quartz Scheduler [" + scheduler.getSchedulerName() + "]");
                schedulerThread.setDaemon(true);
                schedulerThread.start();
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
