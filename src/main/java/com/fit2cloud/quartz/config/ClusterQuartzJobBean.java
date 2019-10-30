package com.fit2cloud.quartz.config;


import com.fit2cloud.quartz.util.ClassUtils;
import com.fit2cloud.quartz.util.QuartzBeanFactory;
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
public class ClusterQuartzJobBean extends QuartzJobBean {
    private Logger logger = LoggerFactory.getLogger(ClusterQuartzFixedDelayJobBean.class);

    private String targetObject;

    private String targetMethod;

    private Object[] params;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        try {
            logger.debug("The scheduled task starts：targetObject={}, targetMethod={}", targetObject, targetMethod);
            Object targetObject = QuartzBeanFactory.getBean(this.targetObject);
            Method m = targetObject.getClass().getMethod(targetMethod, ClassUtils.toClass(params));
            m.invoke(targetObject, params);
            logger.debug("The scheduled task ends normally：targetObject={}, targetMethod={}", this.targetObject, targetMethod);
        } catch (final Exception e) {
            logger.error("The scheduled task execution failed：targetObject=" + targetObject + ", targetMethod=" + targetMethod, e);
        }
    }

    public void setTargetObject(String targetObject) {
        this.targetObject = targetObject;
    }

    public void setTargetMethod(String targetMethod) {
        this.targetMethod = targetMethod;
    }

    public void setParams(Object... params) {
        this.params = params;
    }
}
