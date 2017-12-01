package com.fit2cloud.autocinfigure.config;


import com.zwzx.common.spring.CommonBeanFactory;
import com.zwzx.common.utils.LogUtil;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
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

    private String targetObject;

    private String targetMethod;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            LogUtil.info("定时任务开始：targetObject={}, targetMethod={}", targetObject, targetMethod);
            Object targetObject = CommonBeanFactory.getBean(this.targetObject);
            Method m = targetObject.getClass().getMethod(targetMethod);
            m.invoke(targetObject);
            LogUtil.info("定时任务正常结束：targetObject={}, targetMethod={}", this.targetObject, targetMethod);
        } catch (final Exception e) {
            LogUtil.error("定时任务执行失败：targetObject={}, targetMethod={}", targetObject, targetMethod);
        }
    }

    public void setTargetObject(String targetObject) {
        this.targetObject = targetObject;
    }

    public void setTargetMethod(String targetMethod) {
        this.targetMethod = targetMethod;
    }

}
