package com.fit2cloud.autocinfigure.config;


import com.fit2cloud.autocinfigure.util.CommonBeanFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.lang.reflect.Method;

/**
 * CustomQuartzJobBean代替org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean，解决序列化的问题
 *
 * @author mokun
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class ClusterQuartzFixedDelayJobBean extends QuartzJobBean {
    private Logger logger = LoggerFactory.getLogger(ClusterQuartzJobBean.class);
    private String targetObject;

    private String targetMethod;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        try {
            logger.info("定时任务开始：targetObject={}, targetMethod={}", targetObject, targetMethod);
            Object targetObject = CommonBeanFactory.getBean(this.targetObject);
            Method m = targetObject.getClass().getMethod(targetMethod);
            m.invoke(targetObject);
            logger.info("定时任务正常结束：targetObject={}, targetMethod={}", this.targetObject, targetMethod);
        } catch (final Exception e) {
            logger.error("定时任务执行失败：targetObject={}, targetMethod={}", targetObject, targetMethod);
        }
    }

    public void setTargetObject(String targetObject) {
        this.targetObject = targetObject;
    }

    public void setTargetMethod(String targetMethod) {
        this.targetMethod = targetMethod;
    }

}
